package com.darkfactory.plantpotting.identify.model

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0011 Phase 3 — the `highConfidenceAbstainMargin` gate, exercised on synthetic
 * score vectors (no device). Proves the three contract rows the plan requires:
 *  (i)   high prob but a tiny top1−top2 margin → abstains (downgrades to low-confidence);
 *  (ii)  high prob with a wide margin → stays high-confidence;
 *  (iii) margin `0f` (the shipped default) → identical to today (regression / no-op).
 *
 * The KB/labels/mapping fixtures mirror [ModelScoreMapperTest]; only the abstain margin varies.
 */
class ModelScoreMapperAbstainMarginTest {
    private val labels =
        listOf(
            "Monstera deliciosa", // 0 → monstera-deliciosa
            "Ficus lyrata", // 1 → ficus-lyrata
            "Dracaena trifasciata", // 2 → dracaena-trifasciata
            "Unknown plant A", // 3 → unmapped
            "Unknown plant B", // 4 → unmapped
        )

    private val mapping =
        modelLabelMap(
            "Monstera deliciosa" to "monstera-deliciosa",
            "Ficus lyrata" to "ficus-lyrata",
            "Dracaena trifasciata" to "dracaena-trifasciata",
        )

    private val kb = fixtureKb()

    private fun thresholds(abstainMargin: Float) =
        ModelManifest.Thresholds(
            highConfidencePlain = 0.55f,
            highConfidenceMarginMin = 0.45f,
            highConfidenceMarginDelta = 0.18f,
            topKCandidates = 3,
            highConfidenceAbstainMargin = abstainMargin,
        )

    private fun mapper(abstainMargin: Float) =
        ModelScoreMapper(
            labels = labels,
            mapping = mapping,
            kb = kb,
            thresholds = thresholds(abstainMargin),
        )

    @Test
    fun highProbButNarrowMarginAbstainsToLowConfidence() {
        // best 0.80 clears the 0.55 plain gate, but top1−top2 = 0.10 < the 0.20 abstain margin.
        val scores = floatArrayOf(0.80f, 0.70f, 0.05f, 0.03f, 0.02f)
        val out = mapper(abstainMargin = 0.20f).map(scores)
        assertThat(out.result.lowConfidence).isTrue()
        assertThat(out.result.speciesId).isEmpty()
        // Candidates are still surfaced so the picker shows the same mapped options.
        assertThat(out.candidates.first().speciesId).isEqualTo("monstera-deliciosa")
        // Raw top facts are preserved for the UI/Add-this-plant path.
        assertThat(out.topLabel).isEqualTo("Monstera deliciosa")
        assertThat(out.topProbability).isWithin(1e-6f).of(0.80f)
    }

    @Test
    fun highProbWithWideMarginStaysHighConfidence() {
        // best 0.80, top1−top2 = 0.40 ≥ the 0.20 abstain margin → verdict survives the veto.
        val scores = floatArrayOf(0.80f, 0.40f, 0.05f, 0.03f, 0.02f)
        val out = mapper(abstainMargin = 0.20f).map(scores)
        assertThat(out.result.lowConfidence).isFalse()
        assertThat(out.result.speciesId).isEqualTo("monstera-deliciosa")
    }

    @Test
    fun defaultZeroMarginIsANoOpRegression() {
        // The exact razor-thin-margin vector above, but with the shipped default 0f margin:
        // the verdict must remain high-confidence (byte-for-byte today's behaviour).
        val scores = floatArrayOf(0.80f, 0.70f, 0.05f, 0.03f, 0.02f)
        val out = mapper(abstainMargin = 0f).map(scores)
        assertThat(out.result.lowConfidence).isFalse()
        assertThat(out.result.speciesId).isEqualTo("monstera-deliciosa")
    }

    @Test
    fun abstainMarginAlsoVetoesTheMarginBranch() {
        // best 0.50 (margin-branch territory: ≥0.45 min, top1−top2 = 0.30 ≥ 0.18 delta) would be
        // high-conf via the margin branch — but a 0.40 abstain margin (> 0.30) vetoes it.
        val scores = floatArrayOf(0.50f, 0.20f, 0.12f, 0.10f, 0.08f)
        val accepted = mapper(abstainMargin = 0f).map(scores)
        assertThat(accepted.result.lowConfidence).isFalse() // margin branch accepts at 0f

        val vetoed = mapper(abstainMargin = 0.40f).map(scores)
        assertThat(vetoed.result.lowConfidence).isTrue() // abstain margin overrides
    }

    // ---- fixtures (mirror ModelScoreMapperTest) ----

    private fun modelLabelMap(vararg pairs: Pair<String, String>): ModelLabelMap {
        val entries =
            pairs.associate { (label, kbId) ->
                ModelLabelMap.normalise(label) to ModelLabelMap.Entry(kbSpeciesId = kbId, alias = false)
            }
        val klass = ModelLabelMap::class.java
        val ctor = klass.declaredConstructors.single()
        ctor.isAccessible = true
        return ctor.newInstance(1, "ml/aiy_plants_v1/labels.csv", entries) as ModelLabelMap
    }

    private fun fixtureKb(): KnowledgeBase {
        val archetype =
            Archetype(
                id = "standard",
                displayName = "Standard",
                shortDescription = "",
                recipe = listOf(RecipeIngredient("coir", 100)),
                rationaleTemplate = "Suits {species}.",
                citations = listOf("Brief"),
            )
        val list =
            listOf(
                species("monstera-deliciosa", "Monstera deliciosa", "Swiss cheese plant"),
                species("ficus-lyrata", "Ficus lyrata", "Fiddle-leaf fig"),
                species("dracaena-trifasciata", "Dracaena trifasciata", "Snake plant"),
            )
        return KnowledgeBase(
            archetypes = mapOf("standard" to archetype),
            species = list,
            speciesIndex = list.associateBy { it.id },
        )
    }

    private fun species(
        id: String,
        scientificName: String,
        common: String,
    ): Species =
        Species(
            id = id,
            scientificName = scientificName,
            commonNames = listOf(common),
            aliases = emptyList(),
            mapping = ArchetypeMapping.Single("standard"),
            speciesRationale = "$scientificName.",
            citations = listOf("Brief"),
        )
}
