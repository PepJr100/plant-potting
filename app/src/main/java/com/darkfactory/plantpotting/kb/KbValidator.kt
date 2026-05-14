package com.darkfactory.plantpotting.kb

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.Species

object KbValidator {
    private val FORBIDDEN_PLACEHOLDERS = listOf("TODO", "stub", "lorem", "placeholder")

    fun validate(
        archetypes: List<Archetype>,
        species: List<Species>,
    ): KnowledgeBase {
        validateArchetypes(archetypes)
        val archetypeMap = archetypes.associateBy { it.id }
        validateSpecies(species, archetypeMap)
        return KnowledgeBase(
            archetypes = archetypeMap,
            species = species,
            speciesIndex = buildSpeciesIndex(species),
        )
    }

    private fun validateArchetypes(archetypes: List<Archetype>) {
        val seenIds = mutableSetOf<String>()
        for (a in archetypes) {
            if (!seenIds.add(a.id)) {
                throw KbValidationException("duplicate archetype id: ${a.id}")
            }
            val sum = a.recipe.sumOf { it.proportionPct }
            if (sum != 100) {
                throw KbValidationException("archetype ${a.id}: recipe sums to $sum, expected 100")
            }
            for (ing in a.recipe) {
                if (ing.proportionPct <= 0 || ing.proportionPct > 100) {
                    throw KbValidationException(
                        "archetype ${a.id}: ingredient '${ing.ingredient}' has invalid proportionPct=${ing.proportionPct}",
                    )
                }
            }
            if (!a.rationaleTemplate.contains("{species}")) {
                throw KbValidationException(
                    "archetype ${a.id}: rationaleTemplate is missing required {species} placeholder",
                )
            }
            if (a.citations.isEmpty()) {
                throw KbValidationException("archetype ${a.id}: citations array is empty")
            }
            assertNoPlaceholderText(a.rationaleTemplate, "archetype ${a.id} rationaleTemplate")
        }
    }

    private fun validateSpecies(
        species: List<Species>,
        archetypeMap: Map<String, Archetype>,
    ) {
        val seenIds = mutableSetOf<String>()
        val seenAliasKeys = mutableMapOf<String, String>()
        for (s in species) {
            if (!seenIds.add(s.id)) {
                throw KbValidationException("duplicate species id: ${s.id}")
            }
            if (s.speciesRationale.isBlank()) {
                throw KbValidationException("species ${s.id}: speciesRationale is empty")
            }
            if (s.citations.isEmpty()) {
                throw KbValidationException("species ${s.id}: citations array is empty")
            }
            assertNoPlaceholderText(s.speciesRationale, "species ${s.id} speciesRationale")
            when (val m = s.mapping) {
                is ArchetypeMapping.Single -> {
                    if (m.archetypeId !in archetypeMap) {
                        throw KbValidationException(
                            "species ${s.id}: unknown archetypeId '${m.archetypeId}'",
                        )
                    }
                }
                is ArchetypeMapping.Blend -> {
                    if (m.primaryArchetypeId !in archetypeMap) {
                        throw KbValidationException(
                            "species ${s.id}: unknown primaryArchetypeId '${m.primaryArchetypeId}'",
                        )
                    }
                    if (m.secondaryArchetypeId !in archetypeMap) {
                        throw KbValidationException(
                            "species ${s.id}: unknown secondaryArchetypeId '${m.secondaryArchetypeId}'",
                        )
                    }
                    if (m.primaryPct <= 0 || m.primaryPct >= 100) {
                        throw KbValidationException(
                            "species ${s.id}: blend primaryPct=${m.primaryPct} must be in 1..99",
                        )
                    }
                    if (m.primaryArchetypeId == m.secondaryArchetypeId) {
                        throw KbValidationException(
                            "species ${s.id}: blend primary and secondary archetypes are identical",
                        )
                    }
                }
            }
            val aliasNames = (s.aliases + s.scientificName).distinct()
            for (alias in aliasNames) {
                if (alias.isBlank()) {
                    throw KbValidationException("species ${s.id}: blank alias entry")
                }
                val key = KnowledgeBase.normalise(alias)
                val prior = seenAliasKeys.put(key, s.id)
                if (prior != null && prior != s.id) {
                    throw KbValidationException(
                        "duplicate normalised alias '$key' (between species $prior and ${s.id})",
                    )
                }
            }
        }
    }

    private fun buildSpeciesIndex(species: List<Species>): Map<String, Species> {
        val index = mutableMapOf<String, Species>()
        for (s in species) {
            for (alias in s.aliases + s.scientificName + s.id) {
                if (alias.isBlank()) continue
                index[KnowledgeBase.normalise(alias)] = s
            }
        }
        return index
    }

    private fun assertNoPlaceholderText(
        text: String,
        label: String,
    ) {
        val lowered = text.lowercase()
        for (token in FORBIDDEN_PLACEHOLDERS) {
            if (lowered.contains(token.lowercase())) {
                throw KbValidationException(
                    "$label contains forbidden placeholder token '$token'",
                )
            }
        }
    }
}
