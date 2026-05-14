package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.KbLoader
import com.darkfactory.plantpotting.kb.KbValidator
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test
import java.io.File

/**
 * Runs the engine against the real bundled KB for every species. Fails on any
 * editorial drift: recipe sum != 100, rationale missing the scientific name,
 * or placeholder text leaking through.
 */
class RecommendationGoldenTest {
    private val forbidden = listOf("TODO", "stub", "lorem", "placeholder")

    private val archetypes: List<Archetype> by lazy {
        val f = File("src/main/assets/kb/archetypes.json")
        check(f.exists()) { "archetypes.json missing at ${f.absolutePath}" }
        KbLoader.DefaultJson.decodeFromString(
            ListSerializer(Archetype.serializer()),
            f.readText(Charsets.UTF_8),
        )
    }

    private val species: List<Species> by lazy {
        val f = File("src/main/assets/kb/species.json")
        check(f.exists()) { "species.json missing at ${f.absolutePath}" }
        KbLoader.DefaultJson.decodeFromString(
            ListSerializer(Species.serializer()),
            f.readText(Charsets.UTF_8),
        )
    }

    @Test
    fun everyBundledSpeciesProducesValidRecommendation() {
        val kb = KbValidator.validate(archetypes, species)
        val engine = KbRecommendationEngine(kb)
        for (s in kb.species) {
            val rec = engine.recommend(s.id)
            assertWithMessage("species ${s.id} recipe sum")
                .that(rec.recipe.sumOf { it.proportionPct })
                .isEqualTo(100)
            assertWithMessage("species ${s.id} rationale mentions scientific name")
                .that(rec.rationale)
                .contains(s.scientificName)
            val lowered = rec.rationale.lowercase()
            for (token in forbidden) {
                assertWithMessage("species ${s.id} rationale has forbidden token '$token'")
                    .that(lowered)
                    .doesNotContain(token.lowercase())
            }
        }
    }
}
