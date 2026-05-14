package com.darkfactory.plantpotting.identify

import com.darkfactory.plantpotting.kb.model.KnowledgeBase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Placeholder for PLANTPOTTING-000X to replace with real model — do not depend on this class outside the Hilt binding. */
@Singleton
class StubPlantIdentifier
    @Inject
    constructor(
        private val kb: KnowledgeBase,
    ) : PlantIdentifier {
        private var random: Random = Random(0L)
        private var mode: Mode = Mode.DETERMINISTIC

        // Test-only constructor; not @Inject-annotated, so Hilt ignores it.
        constructor(kb: KnowledgeBase, random: Random, mode: Mode) : this(kb) {
            this.random = random
            this.mode = mode
        }

        enum class Mode { DETERMINISTIC, RANDOM }

        override suspend fun identify(jpeg: ByteArray): IdentificationResult {
            val species =
                when (mode) {
                    Mode.DETERMINISTIC -> deterministicPick()
                    Mode.RANDOM -> kb.species.random(random)
                }
            val source =
                when (mode) {
                    Mode.DETERMINISTIC -> IdSource.STUB_DETERMINISTIC
                    Mode.RANDOM -> IdSource.STUB_RANDOM
                }
            return IdentificationResult(
                speciesId = species.id,
                displayName = species.commonNames.firstOrNull() ?: species.scientificName,
                source = source,
            )
        }

        private fun deterministicPick(): com.darkfactory.plantpotting.kb.model.Species {
            val preferred = kb.species.firstOrNull { it.scientificName == "Monstera deliciosa" }
            return preferred ?: kb.species.first()
        }
    }
