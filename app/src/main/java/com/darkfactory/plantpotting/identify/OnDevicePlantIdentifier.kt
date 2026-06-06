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
                    // PLANTPOTTING-0011 — multi-crop TTA. `preprocessVariants` returns a single
                    // tensor when `ttaCropCount == 1` (the default), so the averaged vector equals
                    // the historical single-crop scores byte-for-byte; >1 averages softmax across crops.
                    val variants = preprocessor.preprocessVariants(jpeg)
                    val scores = averageScores(variants.map { facade.runInference(it) })
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

        /**
         * Element-wise mean of the per-crop softmax vectors. A single vector is returned
         * unchanged (the default `ttaCropCount == 1` path), preserving the historical scores
         * exactly; each input already sums to 1, so the mean does too — no renormalisation.
         */
        private fun averageScores(vectors: List<FloatArray>): FloatArray {
            if (vectors.size == 1) return vectors[0]
            val acc = FloatArray(vectors[0].size)
            for (v in vectors) {
                for (i in acc.indices) acc[i] += v[i]
            }
            val n = vectors.size.toFloat()
            for (i in acc.indices) acc[i] /= n
            return acc
        }
    }
