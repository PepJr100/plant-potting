package com.darkfactory.plantpotting.identify.model

import com.darkfactory.plantpotting.kb.model.Archetype
import com.darkfactory.plantpotting.kb.model.ArchetypeMapping
import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import com.darkfactory.plantpotting.kb.model.RecipeIngredient
import com.darkfactory.plantpotting.kb.model.Species
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PLANTPOTTING-0012 Phase 4 — the pothos↔Pilea boundary gate (Candidate B), exercised on synthetic
 * score vectors (no device). Reproduces the live failure and proves the contract from the plan:
 *  (i)   a confident top-1 = Pilea (the 0.9661, no-second-mass case) routes to the picker — never a
 *        direct Pilea card — once the mapping exists and the gate is on;
 *  (ii)  a true Pilea-dominant row also routes to the picker with Pilea + pothos visible (the
 *        accepted fail-safe cost — a direct Pilea card requires held-out evidence, not granted here);
 *  (iii) ordinary high-confidence species AND a pothos-dominant (top-1 = pothos) result still return
 *        direct cards, and the 0011 abstain behaviour is byte-for-byte unchanged off the boundary;
 *  (iv)  the boundary low-confidence bundle surfaces BOTH pilea-peperomioides and epipremnum-aureum,
 *        the true raw-top is not buried, and candidates are not duplicated when one pair member is
 *        already (or not) in top-k.
 */
class ModelScoreMapperBoundaryGateTest {
    private val labels =
        listOf(
            "Chinese Money Plant (Pilea peperomioides)", // 0 → pilea-peperomioides (boundary trigger)
            "Pothos (Ivy arum)", // 1 → epipremnum-aureum (pair partner)
            "Monstera Deliciosa (Monstera deliciosa)", // 2 → monstera-deliciosa
            "Snake plant (Sanseviera)", // 3 → dracaena-trifasciata
            "Unknown plant", // 4 → unmapped
        )

    private val mapping =
        modelLabelMap(
            "Chinese Money Plant (Pilea peperomioides)" to "pilea-peperomioides",
            "Pothos (Ivy arum)" to "epipremnum-aureum",
            "Monstera Deliciosa (Monstera deliciosa)" to "monstera-deliciosa",
            "Snake plant (Sanseviera)" to "dracaena-trifasciata",
        )

    private val kb = fixtureKb()

    private val thresholds =
        ModelManifest.Thresholds(
            highConfidencePlain = 0.55f,
            highConfidenceMarginMin = 0.45f,
            highConfidenceMarginDelta = 0.18f,
            topKCandidates = 3,
            highConfidenceAbstainMargin = 0.30f, // the shipped 0011 value
        )

    private val pileaPair =
        ModelManifest.BoundaryPair(
            top1KbSpeciesId = "pilea-peperomioides",
            surfaceKbSpeciesIds = listOf("pilea-peperomioides", "epipremnum-aureum"),
        )

    private fun mapper(
        boundaryPairs: List<ModelManifest.BoundaryPair>,
        perSpeciesThresholds: Map<String, Float> = emptyMap(),
    ) = ModelScoreMapper(
        labels = labels,
        mapping = mapping,
        kb = kb,
        thresholds = thresholds,
        perSpeciesThresholds = perSpeciesThresholds,
        boundaryPairs = boundaryPairs,
    )

    // PLANTPOTTING-0013 — the elevated per-species bar that permits a *direct* Pilea card. Strictly
    // above the documented pothos→Pilea ceiling (0.9661), CI-bound > 0.9661 in
    // HousePlantClassMapValidationTest. Matches the shipped manifest value.
    private val tPilea = 0.98f
    private val pileaDirectThreshold = mapOf("pilea-peperomioides" to tPilea)

    // (i) the live failure: pothos misread as Pilea @ 0.9661 (no second-place mass).
    @Test
    fun confidentTop1PileaRoutesToPickerNotADirectPileaCard() {
        val scores = floatArrayOf(0.9661f, 0.01f, 0.01f, 0.005f, 0.005f)

        // Without the gate (and now that Pilea is mapped) this would be a confidently-wrong card:
        val ungated = mapper(emptyList()).map(scores)
        assertThat(ungated.result.lowConfidence).isFalse()
        assertThat(ungated.result.speciesId).isEqualTo("pilea-peperomioides") // the bug, demonstrated

        // With the gate on it must route to the picker and surface NO direct Pilea card:
        val gated = mapper(listOf(pileaPair)).map(scores)
        assertThat(gated.result.lowConfidence).isTrue()
        assertThat(gated.result.speciesId).isEmpty()
        // raw top facts preserved for the UI:
        assertThat(gated.topLabel).isEqualTo("Chinese Money Plant (Pilea peperomioides)")
        assertThat(gated.topProbability).isWithin(1e-6f).of(0.9661f)
        // both pair members visible as candidates:
        val ids = gated.candidates.map { it.speciesId }
        assertThat(ids).containsAtLeast("pilea-peperomioides", "epipremnum-aureum")
    }

    // (ii) a true Pilea-dominant row: same mechanism — fail-safe to the picker, Pilea visible.
    @Test
    fun truePileaDominantRoutesToPickerWithPileaAndPothosVisible() {
        val scores = floatArrayOf(0.88f, 0.06f, 0.03f, 0.02f, 0.01f)
        val out = mapper(listOf(pileaPair)).map(scores)
        assertThat(out.result.lowConfidence).isTrue()
        val ids = out.candidates.map { it.speciesId }
        assertThat(ids).containsAtLeast("pilea-peperomioides", "epipremnum-aureum")
        // Pilea (the true raw-top) is first — not buried below the appended partner.
        assertThat(ids.first()).isEqualTo("pilea-peperomioides")
    }

    // (iii-a) the KEY no-regression: a pothos-dominant result keeps its direct card (gate scoped to Pilea-top-1).
    @Test
    fun pothosDominantStillReturnsADirectPothosCard() {
        val scores = floatArrayOf(0.02f, 0.95f, 0.01f, 0.01f, 0.01f) // top-1 = pothos
        val out = mapper(listOf(pileaPair)).map(scores)
        assertThat(out.result.lowConfidence).isFalse()
        assertThat(out.result.speciesId).isEqualTo("epipremnum-aureum")
    }

    // (iii-b) ordinary non-boundary species unaffected, and abstain behaviour byte-for-byte unchanged.
    @Test
    fun nonBoundarySpeciesAndAbstainBehaviourAreUnchangedByTheGate() {
        val monstera = floatArrayOf(0.02f, 0.01f, 0.95f, 0.01f, 0.01f) // top-1 = monstera
        assertThat(mapper(listOf(pileaPair)).map(monstera).result.speciesId).isEqualTo("monstera-deliciosa")

        // Narrow-margin abstain (0.80 vs 0.70 = 0.10 < 0.30) must still abstain, identically with/without gate.
        val narrow = floatArrayOf(0.02f, 0.01f, 0.80f, 0.70f, 0.01f) // monstera vs snake, narrow
        val gated = mapper(listOf(pileaPair)).map(narrow)
        val ungated = mapper(emptyList()).map(narrow)
        assertThat(gated.result.lowConfidence).isTrue()
        assertThat(ungated.result.lowConfidence).isTrue()
        assertThat(gated.result.speciesId).isEqualTo(ungated.result.speciesId)
    }

    // (iv) candidate bundle: pothos OUTSIDE top-k must be appended (surfaced) without duplication.
    @Test
    fun boundaryBundleSurfacesPothosEvenWhenOutsideTopK() {
        // ranked: pilea(.90) > monstera(.05) > snake(.04) > pothos(.005) > unknown(.005)
        val scores = floatArrayOf(0.90f, 0.005f, 0.05f, 0.04f, 0.005f)
        val out = mapper(listOf(pileaPair)).map(scores)
        assertThat(out.result.lowConfidence).isTrue()
        val ids = out.candidates.map { it.speciesId }
        // pothos was rank 4 (outside top-3) but is appended:
        assertThat(ids).contains("epipremnum-aureum")
        assertThat(ids).contains("pilea-peperomioides")
        // pilea (true raw-top) first, not buried:
        assertThat(ids.first()).isEqualTo("pilea-peperomioides")
        // no duplication of either pair member:
        assertThat(ids.count { it == "pilea-peperomioides" }).isEqualTo(1)
        assertThat(ids.count { it == "epipremnum-aureum" }).isEqualTo(1)
    }

    // ====================================================================================
    // PLANTPOTTING-0013 Phase A3 — conditional direct Pilea card behind the elevated T_pilea bar.
    // ====================================================================================

    // (a) the load-bearing safety case: a pothos misread as Pilea @ 0.9661 (no second-place mass)
    // routes to the picker EVEN WITH the direct-card threshold configured — proving T_pilea > 0.9661
    // is enforced and the known failure can never reach a direct Pilea card.
    @Test
    fun pothosMisreadAsPileaAt09661RoutesToPickerEvenWithDirectThresholdConfigured() {
        val scores = floatArrayOf(0.9661f, 0.01f, 0.01f, 0.005f, 0.005f)
        val out = mapper(listOf(pileaPair), pileaDirectThreshold).map(scores)
        assertThat(out.result.lowConfidence).isTrue()
        assertThat(out.result.speciesId).isEmpty()
        val ids = out.candidates.map { it.speciesId }
        assertThat(ids).containsAtLeast("pilea-peperomioides", "epipremnum-aureum")
    }

    // (b) a real-Pilea-dominant row ABOVE T_pilea (0.99) yields a DIRECT Pilea card.
    @Test
    fun realPileaAboveTPileaYieldsADirectPileaCard() {
        val scores = floatArrayOf(0.99f, 0.005f, 0.003f, 0.001f, 0.001f)
        val out = mapper(listOf(pileaPair), pileaDirectThreshold).map(scores)
        assertThat(out.result.lowConfidence).isFalse()
        assertThat(out.result.speciesId).isEqualTo("pilea-peperomioides")
    }

    // (c) a Pilea-dominant row in the conservative MIDDLE BAND (0.9661 < score < T_pilea) still
    // routes to the picker with Pilea + pothos visible.
    @Test
    fun pileaInMiddleBandStillRoutesToPickerWithBothVisible() {
        val scores = floatArrayOf(0.97f, 0.01f, 0.01f, 0.005f, 0.005f) // 0.9661 < 0.97 < 0.98
        val out = mapper(listOf(pileaPair), pileaDirectThreshold).map(scores)
        assertThat(out.result.lowConfidence).isTrue()
        val ids = out.candidates.map { it.speciesId }
        assertThat(ids).containsAtLeast("pilea-peperomioides", "epipremnum-aureum")
        assertThat(ids.first()).isEqualTo("pilea-peperomioides")
    }

    // (d) the dormant-safe default: with NO per-species threshold and a boundary pair present, a
    // confident Pilea-top-1 behaves EXACTLY like today's strict-picker (byte-for-byte 0012).
    @Test
    fun emptyThresholdPathIsByteForByteTheStrictPicker() {
        val scores = floatArrayOf(0.99f, 0.005f, 0.003f, 0.001f, 0.001f) // would be a direct card IF T_pilea set
        val strictPicker = mapper(listOf(pileaPair), emptyMap()).map(scores)
        assertThat(strictPicker.result.lowConfidence).isTrue()
        assertThat(strictPicker.result.speciesId).isEmpty()
        // Same scores WITH the threshold → direct card (proves the only activation is the threshold).
        val withThreshold = mapper(listOf(pileaPair), pileaDirectThreshold).map(scores)
        assertThat(withThreshold.result.lowConfidence).isFalse()
        assertThat(withThreshold.result.speciesId).isEqualTo("pilea-peperomioides")
    }

    // (e) regression: the Pilea direct-card threshold is scoped to Pilea — a NON-boundary species is
    // unaffected by it, and off-boundary narrow-margin abstain is byte-for-byte unchanged. (The
    // per-species *plain* override semantics themselves are covered by ModelScoreMapperPerSpeciesThresholdTest.)
    @Test
    fun pileaDirectThresholdDoesNotPerturbNonBoundarySpeciesOrAbstain() {
        // A confident monstera is a direct card identically with/without the Pilea direct threshold.
        val monstera = floatArrayOf(0.02f, 0.01f, 0.95f, 0.01f, 0.01f) // top-1 = monstera @ 0.95
        val withPilea = mapper(listOf(pileaPair), pileaDirectThreshold).map(monstera)
        val without = mapper(listOf(pileaPair), emptyMap()).map(monstera)
        assertThat(withPilea.result.lowConfidence).isFalse()
        assertThat(withPilea.result.speciesId).isEqualTo("monstera-deliciosa")
        assertThat(withPilea.result.speciesId).isEqualTo(without.result.speciesId)

        // Off-boundary narrow-margin abstain (0.80 vs 0.70 = 0.10 < 0.30) is identical either way.
        val narrow = floatArrayOf(0.02f, 0.01f, 0.80f, 0.70f, 0.01f) // monstera vs snake, narrow
        val narrowWithPilea = mapper(listOf(pileaPair), pileaDirectThreshold).map(narrow)
        val narrowWithout = mapper(listOf(pileaPair), emptyMap()).map(narrow)
        assertThat(narrowWithPilea.result.lowConfidence).isTrue()
        assertThat(narrowWithout.result.lowConfidence).isTrue()
        assertThat(narrowWithPilea.result.speciesId).isEqualTo(narrowWithout.result.speciesId)
    }

    // ---- fixtures (mirror ModelScoreMapperAbstainMarginTest) ----

    private fun modelLabelMap(vararg pairs: Pair<String, String>): ModelLabelMap {
        val entries =
            pairs.associate { (label, kbId) ->
                ModelLabelMap.normalise(label) to ModelLabelMap.Entry(kbSpeciesId = kbId, alias = false)
            }
        val klass = ModelLabelMap::class.java
        val ctor = klass.declaredConstructors.single()
        ctor.isAccessible = true
        return ctor.newInstance(1, "ml/house_plant_species_mobilenetv2/labels.csv", entries) as ModelLabelMap
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
                species("pilea-peperomioides", "Pilea peperomioides", "Chinese money plant"),
                species("epipremnum-aureum", "Epipremnum aureum", "Pothos"),
                species("monstera-deliciosa", "Monstera deliciosa", "Swiss cheese plant"),
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
