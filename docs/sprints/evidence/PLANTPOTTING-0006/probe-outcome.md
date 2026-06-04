# PLANTPOTTING-0006 — `crassula-ovata` GMD probe outcome

**Device:** `pixel6Api34` Gradle Managed Device (AOSP android-34), warm AVD.
**Test:** `OnDeviceModelRealInterpreterTest.probeCrassula` (temporary probe, removed before commit per 0005 §5.5).
**Fixture:** `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg` (CC0, 480×480 q80).
**Run:** `:app:pixel6Api34DebugAndroidTest` — 3/3 tests GREEN, BUILD SUCCESSFUL in 58s.

## Raw measured line

```
PROBE speciesId= lowConf=true source=ON_DEVICE_MODEL :: mappedCandidates=[crassula-ovata=0.1055 | monstera-deliciosa=0.0000]
```

## Interpretation

| Field | Value |
|---|---|
| `result.speciesId` | `""` (empty — the low-confidence path emits no species id) |
| `result.lowConfidence` | `true` |
| `result.source` | `ON_DEVICE_MODEL` (the real interpreter ran end-to-end) |
| Top mapped candidate | `crassula-ovata` @ **p ≈ 0.1055** |
| Other mapped candidate | `monstera-deliciosa` @ p ≈ 0.0000 |

The real jade photo ranks `crassula-ovata` as the **top in-vocab mapped candidate**, but at
only **p ≈ 0.1055** — far below the global `high_confidence_plain = 0.55` (deficit ≈ 0.44) and
below `high_confidence_margin_min = 0.45`. So neither the direct nor the margin high-confidence
path fires and the result routes **low-confidence** to `LowConfidencePicker`.

This contrasts sharply with the Monstera probe (0005 §5.5: top-1 = `monstera-deliciosa` @ 0.8984,
clears cleanly). The AIY V1/3 vocabulary contains `Crassula ovata`, but the model does **not**
confidently recognise this canonical jade photograph as that class.

`mostRecentCandidates` surfaces only the **mapped** (in-vocab KB) classes; the overall top-1 label
(across all 2102 classes) is not exposed by `OnDevicePlantIdentifier.identify` and is likely an
unmapped non-houseplant class. Either way the calibration conclusion is unchanged.

## Consequences

- **Phase 2 assertion → documented honest fallback** (not the preferred `speciesId == "crassula-ovata"
  && !lowConfidence`). The committed assertion pins the *measured* reality: `source == ON_DEVICE_MODEL`,
  `lowConfidence == true`, `speciesId` empty, and `crassula-ovata` is the top mapped candidate.
- **Phase 3 seeding → NOT warranted.** Closing a 0.1055 → 0.55 gap with a per-species override would
  mean declaring a ~10%-confidence prediction "high confidence" — egregious overfitting and a direct
  violation of the §5.6 anti-overfit prohibition. `per_species_thresholds` ships empty.
