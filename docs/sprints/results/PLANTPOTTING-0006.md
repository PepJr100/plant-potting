# PLANTPOTTING-0006 — Results

**Status:** `in-progress` (executor: opus). Second in-vocab calibration probe
(`crassula-ovata`) + the two 0005-review UX fixes. This doc collects the
multi-species calibration evidence next to the 0005 Monstera result so the V0.1
sweep reads as one story.

## Multi-species calibration sweep (V0.1)

The AIY V1/3 vocabulary overlaps the retail-houseplant KB on exactly **two** of 18
mapping keys: `Monstera deliciosa` and `Crassula ovata`. Both are now probed against
real CC-licensed photographs on the `pixel6Api34` GMD.

| Species | Fixture (license) | Top mapped p | Route | Assertion form |
|---|---|---|---|---|
| `monstera-deliciosa` | `monstera-deliciosa.jpg` (CC BY-SA 3.0) | **0.8984** | high-confidence direct | preferred (0005 §5.6) |
| `crassula-ovata` | `crassula-ovata.jpg` (CC0) | **0.1055** | **low-confidence** | documented honest fallback |

**Conclusion:** the two in-vocab species sit at opposite ends. Monstera clears the
`high_confidence_plain = 0.55` global cleanly; jade does not come close (0.1055, below even
`high_confidence_margin_min = 0.45`). The model contains the `Crassula ovata` label but does
not confidently recognise this canonical jade photo as that class. `per_species_thresholds`
ships **empty** — Monstera needs no override (clears outright), and seeding jade would mean
declaring a ~10%-confidence prediction "high confidence" (egregious overfitting, §5.6).

## Per-phase summary

### Phase 4 — UX fix A: subtitle jargon (commit `5793b9a`)

`R.string.low_conf_subtitle` rewritten from "This model recognises a limited plant
vocabulary…" to **"We're best at common houseplants — confirm or pick from the list below."**
`LowConfidencePickerSubtitleContractTest` strengthened from a presence-only lock to a copy
lock (pins the new wording, asserts "model" is gone). TDD: test red against the old string,
green after the rewrite.

### Phase 5 — UX fix B: `(0%)` chips (commit `5793b9a`)

`LowConfidencePickerScreen` chip-text builder drops the `(x%)` suffix when `probabilityPct`
floors to 0 (a degenerate capture rendered "Jade plant (0%)", which reads as broken). Suffix
kept for all `>= 1%`; the candidate stays selectable (presentational change only).
`LowConfidencePickerScreenTest` gains a zero-suffix (present + clickable) case and a
normal-suffix case. The Phase 5 conditional (`FakeFixedIdentifier` split) was **not forced**
— the fix added no fake knob (logged in `ml-mapping-notes.md`).

### Phase 1 — `crassula-ovata` fixture (commit `da673f9`)

CC0 photo "Jade Plant, Crassula ovata IMG 3632" by S.G.S. (Wikimedia Commons),
centre-cropped + scaled to 480×480 JPEG q80 (~30 KB, baseline) — `file(1)` output matches
`monstera-deliciosa.jpg` exactly. Visually verified an unambiguous jade plant. Provenance
block appended to `identify-fixtures/LICENSE.txt` mirroring the Monstera block. Asset is
androidTest-only.

### Phase 2 — probe, record, assert

Temporary `probeCrassula` test (removed before commit, per 0005 §5.5) surfaced the raw
numbers on the GMD:

```
PROBE speciesId= lowConf=true source=ON_DEVICE_MODEL :: mappedCandidates=[crassula-ovata=0.1055 | monstera-deliciosa=0.0000]
```

The committed assertion is the **documented honest fallback**
(`OnDeviceModelRealInterpreterTest.realCrassulaPhotoRanksJadeTopMappedButRoutesLowConfidence`):
asserts `source == ON_DEVICE_MODEL`, `lowConfidence == true`, `speciesId` empty, and that
`crassula-ovata` is the top mapped candidate (mapping + scoring wiring exercised end-to-end).
The preferred form (`speciesId == "crassula-ovata" && !lowConfidence`) does **not** hold and
was not committed — the probe number forbade it. Evidence:
`docs/sprints/evidence/PLANTPOTTING-0006/` (probe logcat, probe-outcome.md, green GMD XML).

GMD run: `:app:pixel6Api34DebugAndroidTest` filtered to `OnDeviceModelRealInterpreterTest` —
**3/3 GREEN** (Monstera high-conf, crassula low-conf, binding guard), BUILD SUCCESSFUL.

### Phase 3 — `perSpeciesThresholds` decision

**Not warranted — ships empty.** Deciding number: crassula top mapped p = **0.1055**. See the
sweep conclusion above and `ml-mapping-notes.md` §Calibration-provenance §PLANTPOTTING-0006.
The cross-class Monstera re-verify is a **no-op** because the map stayed empty (the Monstera
assertion nonetheless re-ran green in the same GMD invocation, @ 0.8984, via the global path).
Both conditional cleanups (margin semantics; `FakeFixedIdentifier` split) were **not forced**
and are logged, not built.

### Phase 6 — close-out gates

Run cheap-to-expensive; all GREEN:

| Gate | Result |
|---|---|
| Focused JVM (`LowConfidencePickerScreenTest`, `LowConfidencePickerSubtitleContractTest`) | GREEN |
| Threshold contract tests | n/a — no seeding, map empty (skipped by design) |
| Instrumented `OnDeviceModelRealInterpreterTest` (Monstera + crassula) on `pixel6Api34` | **3/3 GREEN** |
| Full JVM unit suite (`testDebugUnitTest`) | GREEN |
| `ktlintCheck` | GREEN |
| Stub-isolation (`scripts/check-stub-isolation.sh`) | `stub isolation OK` |
| Network-free (`verifyNoNetworking`) | GREEN |
| `integration-flow.ps1` cold / warm / buildonly | all `Integration manifest diff passed.` |

Audit greps clean: 0 `println` / probe / `@Ignore` in `app/src/androidTest/`; the temporary
probe was removed and replaced by the committed honest assertion. Integration-flow transcripts:
`docs/sprints/evidence/PLANTPOTTING-0006/transcript-{A-cold,B-warm,C-buildonly}.txt`; instrumented
GMD evidence: `gmd-OnDeviceModelRealInterpreterTest-green.xml` + `gmd-green-summary.txt`.

**V0.1 multi-species sweep: COMPLETE.** Both AIY V1/3 ↔ KB in-vocab overlaps
(`monstera-deliciosa`, `crassula-ovata`) are probed against real photos with committed
accuracy-bearing assertions. `per_species_thresholds` ships empty by evidence-driven design.
Ledger flipped to `done`.
