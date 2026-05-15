package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KbRecommendationEngine
    @Inject
    constructor(
        private val kb: KnowledgeBase,
    ) : RecommendationEngine {
        override fun recommend(speciesId: String): Recommendation {
            val species =
                kb.findSpecies(speciesId)
                    ?: throw IllegalArgumentException("unknown species: $speciesId")
            return when (val mapping = species.mapping) {
                is ArchetypeMapping.Single -> single(species, mapping)
                is ArchetypeMapping.Blend -> blend(species, mapping)
            }
        }

        override fun recommendByArchetype(archetypeId: String): Recommendation {
            val archetype =
                kb.archetypes[archetypeId]
                    ?: throw IllegalArgumentException("unknown archetype: $archetypeId")
            // Strip the `{species}` placeholder out of the archetype template so the
            // rationale reads as a general statement, not a species-specific one.
            val rationale = archetype.rationaleTemplate.replace("{species}", "this plant")
            return Recommendation(
                archetypeName = archetype.displayName,
                recipe = archetype.recipe,
                rationale = rationale,
                isBlend = false,
            )
        }

        private fun single(
            species: Species,
            mapping: ArchetypeMapping.Single,
        ): Recommendation {
            val archetype =
                kb.archetypes[mapping.archetypeId]
                    ?: throw IllegalStateException(
                        "species ${species.id}: archetype '${mapping.archetypeId}' missing from KB",
                    )
            return Recommendation(
                archetypeName = archetype.displayName,
                recipe = archetype.recipe,
                rationale = buildSingleRationale(species, archetype),
                isBlend = false,
            )
        }

        private fun blend(
            species: Species,
            mapping: ArchetypeMapping.Blend,
        ): Recommendation {
            val primary =
                kb.archetypes[mapping.primaryArchetypeId]
                    ?: throw IllegalStateException(
                        "species ${species.id}: primary archetype '${mapping.primaryArchetypeId}' missing",
                    )
            val secondary =
                kb.archetypes[mapping.secondaryArchetypeId]
                    ?: throw IllegalStateException(
                        "species ${species.id}: secondary archetype '${mapping.secondaryArchetypeId}' missing",
                    )
            val recipe =
                blendRecipes(
                    primary = primary.recipe,
                    secondary = secondary.recipe,
                    primaryPct = mapping.primaryPct,
                )
            return Recommendation(
                archetypeName = "${primary.displayName} / ${secondary.displayName} blend",
                recipe = recipe,
                rationale = buildBlendRationale(species, primary, secondary, mapping.primaryPct),
                isBlend = true,
            )
        }

        private fun buildSingleRationale(
            species: Species,
            archetype: Archetype,
        ): String {
            val filled = archetype.rationaleTemplate.replace("{species}", species.scientificName)
            return "${species.speciesRationale} $filled"
        }

        private fun buildBlendRationale(
            species: Species,
            primary: Archetype,
            secondary: Archetype,
            primaryPct: Int,
        ): String {
            val primaryFilled = primary.rationaleTemplate.replace("{species}", species.scientificName)
            val secondaryFilled = secondary.rationaleTemplate.replace("{species}", species.scientificName)
            val secondaryPct = 100 - primaryPct
            return buildString {
                append(species.speciesRationale)
                append(' ')
                append(
                    "This recommendation is a $primaryPct/$secondaryPct blend of " +
                        "${primary.displayName} and ${secondary.displayName}. ",
                )
                append(primaryFilled)
                append(' ')
                append(secondaryFilled)
            }
        }

        private fun blendRecipes(
            primary: List<RecipeIngredient>,
            secondary: List<RecipeIngredient>,
            primaryPct: Int,
        ): List<RecipeIngredient> {
            val pFactor = primaryPct / 100.0
            val sFactor = (100 - primaryPct) / 100.0
            val accum = linkedMapOf<String, Double>()
            val notes = mutableMapOf<String, String?>()
            for (ing in primary) {
                accum.merge(ing.ingredient, ing.proportionPct * pFactor) { a, b -> a + b }
                notes.putIfAbsent(ing.ingredient, ing.notes)
            }
            for (ing in secondary) {
                accum.merge(ing.ingredient, ing.proportionPct * sFactor) { a, b -> a + b }
                notes.putIfAbsent(ing.ingredient, ing.notes)
            }
            return normaliseToIntPercentages(accum, notes)
        }

        private fun normaliseToIntPercentages(
            accum: LinkedHashMap<String, Double>,
            notes: Map<String, String?>,
        ): List<RecipeIngredient> {
            val floors = accum.mapValues { (_, v) -> v.toInt() }
            val flooredSum = floors.values.sum()
            val remainder = 100 - flooredSum
            val biggest =
                accum.entries.maxByOrNull { it.value }?.key
                    ?: error("blend produced empty recipe")
            return accum.map { (name, _) ->
                val base = floors[name] ?: 0
                val pct = if (name == biggest) base + remainder else base
                RecipeIngredient(ingredient = name, proportionPct = pct, notes = notes[name])
            }
        }
    }
