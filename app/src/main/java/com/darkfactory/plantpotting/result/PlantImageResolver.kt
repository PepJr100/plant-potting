package com.darkfactory.plantpotting.result

import androidx.annotation.DrawableRes
import com.darkfactory.plantpotting.R

/**
 * PLANTPOTTING-0010 Phase 6 / D4 — resolves a KB species id to a bundled **CC0/PD** reference image
 * (downscaled WebP under `res/drawable-nodpi/`), falling back to an authored placeholder vector when
 * no clean image is bundled.
 *
 * The [images] registry starts empty: sprint 0008 found license-clean houseplant imagery is scarce
 * (~1–6.8%), so we ship the infrastructure + placeholder and add verified images incrementally. Every
 * entry added here MUST have a matching row in `docs/licenses/reference-images.md` (CI-cross-checked
 * by `ReferenceImageManifestTest`), and the file must live in `res/drawable-nodpi/`.
 */
object PlantImageResolver {
    @DrawableRes
    val placeholder: Int = R.drawable.ic_plant_placeholder

    /**
     * speciesId → bundled CC0/PD WebP drawable. Sourced from Wikimedia Commons (CC0 photos + PD
     * botanical plates) by `scripts/source-reference-images.ps1`; every entry has an attribution row
     * in `docs/licenses/reference-images.md`. Species without a license-clean image fall back to the
     * placeholder (PD-only filter skipped: philodendron-hederaceum/pink-princess, phalaenopsis,
     * alocasia, dracaena, hedera-helix, asplenium-nidus, cycas-revoluta, yucca, ctenanthe).
     */
    private val images: Map<String, Int> =
        mapOf(
            "monstera-deliciosa" to R.drawable.monstera_deliciosa,
            "monstera-adansonii" to R.drawable.monstera_adansonii,
            "epipremnum-aureum" to R.drawable.epipremnum_aureum,
            "spathiphyllum-wallisii" to R.drawable.spathiphyllum_wallisii,
            "ficus-elastica" to R.drawable.ficus_elastica,
            "ficus-lyrata" to R.drawable.ficus_lyrata,
            "dracaena-trifasciata" to R.drawable.dracaena_trifasciata,
            "zamioculcas-zamiifolia" to R.drawable.zamioculcas_zamiifolia,
            "crassula-ovata" to R.drawable.crassula_ovata,
            "chlorophytum-comosum" to R.drawable.chlorophytum_comosum,
            "saintpaulia-ionantha" to R.drawable.saintpaulia_ionantha,
            "goeppertia-orbifolia" to R.drawable.goeppertia_orbifolia,
            "hoya-carnosa" to R.drawable.hoya_carnosa,
            "aglaonema" to R.drawable.aglaonema,
            "anthurium-andraeanum" to R.drawable.anthurium_andraeanum,
            "dieffenbachia" to R.drawable.dieffenbachia,
            "aloe-vera" to R.drawable.aloe_vera,
            "kalanchoe" to R.drawable.kalanchoe,
            "maranta-leuconeura" to R.drawable.maranta_leuconeura,
            "nephrolepis-exaltata" to R.drawable.nephrolepis_exaltata,
            "pachira-aquatica" to R.drawable.pachira_aquatica,
            "dypsis-lutescens" to R.drawable.dypsis_lutescens,
            "tradescantia" to R.drawable.tradescantia,
            "schefflera" to R.drawable.schefflera,
            "euphorbia-pulcherrima" to R.drawable.euphorbia_pulcherrima,
            "dionaea-muscipula" to R.drawable.dionaea_muscipula,
            "chamaedorea-elegans" to R.drawable.chamaedorea_elegans,
            "strelitzia-reginae" to R.drawable.strelitzia_reginae,
            "aspidistra-elatior" to R.drawable.aspidistra_elatior,
            "asparagus-setaceus" to R.drawable.asparagus_setaceus,
            "begonia" to R.drawable.begonia,
            "hypoestes-phyllostachya" to R.drawable.hypoestes_phyllostachya,
            "beaucarnea-recurvata" to R.drawable.beaucarnea_recurvata,
            "schlumbergera-bridgesii" to R.drawable.schlumbergera_bridgesii,
        )

    /** The drawable to render for [speciesId] — a real reference image if bundled, else the placeholder. */
    @DrawableRes
    fun drawableFor(speciesId: String): Int = images[speciesId] ?: placeholder

    /** True iff a real (non-placeholder) reference image is bundled for [speciesId]. */
    fun hasRealImage(speciesId: String): Boolean = images.containsKey(speciesId)
}
