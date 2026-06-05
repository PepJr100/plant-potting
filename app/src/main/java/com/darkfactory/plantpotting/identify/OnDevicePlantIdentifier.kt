package com.darkfactory.plantpotting.identify

import com.darkfactory.plantpotting.identify.model.Candidate
import com.darkfactory.plantpotting.identify.model.CandidateProvider
import com.darkfactory.plantpotting.identify.model.ImagePreprocessor
import com.darkfactory.plantpotting.identify.model.InterpreterFacade
import com.darkfactory.plantpotting.identify.model.ModelScoreMapper
import com.darkfactory.plantpotting.identify.model.TopPrediction
import com.darkfactory.plantpotting.identify.model.UnmappedTopProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [PlantIdentifier] backed by an on-device TFLite classifier.
 *
 * Pipeline: decode → preprocess → run inference on [dispatcher] (Dispatchers.Default in
 * production via the `@InferenceDispatcher` Hilt qualifier) → score-mapping per §4.3 →
 * emit [IdentificationResult] with `source = IdSource.ON_DEVICE_MODEL` and an optional
 * `lowConfidence = true` flag.
 *
 * Hard failures (model load failure, malformed JPEG, native interpreter error) are
 * re-raised as [IdentificationFailureException] — they are *not* converted to a
 * low-confidence verdict (PLANTPOTTING-0003 §4.3 distinction; codex critique 2.2 of claude).
 *
 * Top-K candidates ride on this class via the [CandidateProvider] side-channel; the
 * `PlantIdentifier` seam stays minimal per §4.4.
 */
@Singleton
class OnDevicePlantIdentifier
    @Inject
    constructor(
        private val preprocessor: ImagePreprocessor,
        private val facade: InterpreterFacade,
        private val mapper: ModelScoreMapper,
        @InferenceDispatcher private val dispatcher: CoroutineDispatcher,
    ) : PlantIdentifier,
        CandidateProvider,
        UnmappedTopProvider {
        @Volatile
        private var lastCandidates: List<Candidate> = emptyList()

        @Volatile
        private var lastTop: TopPrediction? = null

        override val mostRecentCandidates: List<Candidate>
            get() = lastCandidates

        override val mostRecentTop: TopPrediction?
            get() = lastTop

        override suspend fun identify(jpeg: ByteArray): IdentificationResult =
            withContext(dispatcher) {
                try {
                    val preprocessed = preprocessor.preprocess(jpeg)
                    val scores = facade.runInference(preprocessed)
                    val mapped = mapper.map(scores)
                    lastCandidates = mapped.candidates
                    lastTop =
                        TopPrediction(
                            modelClassLabel = mapped.topLabel,
                            probabilityPct = (mapped.topProbability * 100).toInt().coerceIn(0, 100),
                            isConfidentUnmapped = mapped.topIsConfidentUnmapped,
                        )
                    mapped.result
                } catch (e: IdentificationFailureException) {
                    lastCandidates = emptyList()
                    lastTop = null
                    throw e
                } catch (e: Throwable) {
                    lastCandidates = emptyList()
                    lastTop = null
                    throw IdentificationFailureException(
                        "On-device identifier failed: ${e.message}",
                        e,
                    )
                }
            }
    }
