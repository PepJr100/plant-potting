package com.darkfactory.plantpotting.identify.model

/**
 * A model-resolved KB species candidate plus its predicted probability.
 *
 * Candidates ride on the camera-flow navigation event (`NavCommand.LowConfidence`) — they
 * are *not* fields on `IdentificationResult`, which stays minimal per PLANTPOTTING-0003 §4.4.
 *
 * `probability` is the raw softmax probability (0..1) from the interpreter; the UI displays
 * it as an integer percentage in the picker (see §6.4).
 */
data class Candidate(
    val speciesId: String,
    val displayName: String,
    val probability: Float,
)
