package com.darkfactory.plantpotting.recommend

import com.darkfactory.plantpotting.kb.KbLoader
import com.darkfactory.plantpotting.kb.KbValidator
import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test

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
            assertThat(rec.recipe.sumOf { it.proportionPct })
                .named("species ${s.id} recipe sum")
                .isEqualTo(100)
            assertThat(rec.rationale)
                .named("species ${s.id} rationale mentions scientific name")
                .contains(s.scientificName)
            val lowered = rec.rationale.lowercase()
            for (token in forbidden) {
                assertThat(lowered)
                    .named("species ${s.id} rationale has forbidden token '$token'")
                    .doesNotContain(token.lowercase())
            }
        }
    }
}
