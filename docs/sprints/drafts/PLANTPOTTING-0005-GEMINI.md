# PLANTPOTTING-0005 — Polish, Calibration, and Un-deferral

**Intent:** Polish the post-shutter experience, calibrate confidence thresholds, and un-defer critical test infrastructure tasks after the on-device ML bugs were resolved in PLANTPOTTING-0003/0004.

---

## §1 Goals

- **Post-shutter Polish:** Make `LowConfidencePicker` and `CameraUiState.Failure` feel like finished, high-quality surfaces rather than skeletal stubs.
- **Confidence Calibration:** Transition from "procedural smoke fixtures" to real photographic evidence and per-class threshold tuning.
- **Infrastructure Cleanup:** Complete the deferred Hilt `@BindValue` migration and remove the global `TestIdentifyModule`.
- **Test Completion:** Re-enable `PermissionDeniedFlowTest` and add `LowConfidenceFlowTest` to close the instrumentation gap.
- **Documentation:** Establish `docs/ROADMAP.md` as the source of truth for project trajectory.

---

## §2 Scope Boundaries

### In-Scope
- Refactoring `LowConfidencePickerScreen` for better usability (search visibility, "didn't find your plant" affordance).
- Prominent failure UI in `CameraScreen` (Snackbar or Material banner).
- Migrating six instrumentation tests from global `TestIdentifyModule` to local `@BindValue` overrides.
- Implementing `LowConfidenceFlowTest` (instrumentation).
- Fixing and un-ignoring `PermissionDeniedFlowTest`.
- Adding per-class threshold support to `ModelManifest.kt` and `ModelScoreMapper.kt`.
- Swapping the synthetic Monstera fixture for a real CC-licensed photo and adding accuracy assertions.
- Creating `docs/ROADMAP.md`.

### Out-of-Scope (Non-Goals)
- Model swap or training.
- INT8/GPU/NNAPI delegates (deferred to 0006+).
- Net-new screens (e.g., dedicated "Settings" or "History").
- Backend integration (the app remains offline-only).

---

## §3 Task List

### §3.1 Infrastructure & Documentation
- [ ] Create `docs/ROADMAP.md` with current state, layer status, and upcoming milestones.
- [ ] Remove `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt`.
- [ ] Migrate `EndToEndFlowTest.kt` to use `@BindValue` for `PlantIdentifier` override.
- [ ] Migrate other instrumentation tests (total 6) to use `@BindValue` where they previously relied on global `TestIdentifyModule`.
- [ ] Verify `OnDeviceModelRealInterpreterTest.kt` still functions correctly without the global module.

### §3.2 UI/UX Polish
- [ ] Polish `LowConfidencePickerScreen`:
    - [ ] Add a prominent "Search for your plant" header/affordance.
    - [ ] Add a "None of these look like my plant" button that navigates to `ArchetypePickerScreen`.
    - [ ] Improve list item styling (elevation, spacing, clear species names).
- [ ] Polish `CameraUiState.Failure` handling:
    - [ ] Implement a `Snackbar` or `Material Banner` for failures (e.g., TFLite errors).
    - [ ] Ensure the failure message is actionable (e.g., "Try again" or "Check lighting").
    - [ ] Remove the skeletal `TopCenter` text overlay from `CameraScreen`.

### §3.3 Confidence Calibration
- [ ] Update `ModelManifest.kt` to support a `per_class_thresholds` map in `model_manifest.json`.
- [ ] Update `ModelScoreMapper.kt` to respect per-class thresholds if present, falling back to `high_confidence_plain`.
- [ ] Replace `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg` with a real CC-licensed photograph.
- [ ] Update `OnDeviceModelRealInterpreterTest.kt` with an accuracy assertion (e.g., score for *Monstera deliciosa* > 0.6).
- [ ] Add a manual "Calibration" note to `docs/kb/ml-mapping-notes.md` explaining how thresholds were determined.

### §3.4 Testing & Validation
- [ ] Implement `LowConfidenceFlowTest.kt` (drives a flow that hits the `LowConfidencePicker` path).
- [ ] Re-enable `PermissionDeniedFlowTest.kt`:
    - [ ] Update `FakeGuardStateRule` or use `FakeCameraPermissionGuard` to simulate `PermanentlyDenied` state.
    - [ ] Verify `ACTION_APPLICATION_DETAILS_SETTINGS` intent fires when "Grant" is clicked in denied state.
- [ ] Run full gate chain: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest verifyNoNetworking`.
- [ ] Run `scripts/integration-flow.ps1` and verify manifest consistency.

---

## §4 Sequencing

1. **Phase 1: Foundation.** `ROADMAP.md` and Hilt `@BindValue` migration. This ensures the test infrastructure is "warm" for the rest of the sprint.
2. **Phase 2: UI Polish.** Revamp `LowConfidencePicker` and `Failure` states. These are the most visible changes for a reviewer.
3. **Phase 3: Calibration.** Land the real photo fixture and per-class threshold logic.
4. **Phase 4: Closure.** Final instrumentation tests (`LowConfidenceFlowTest`, `PermissionDeniedFlowTest`) and full gate validation.

---

## §5 Risks & Mitigations

- **Risk:** `PermanentlyDenied` state is notoriously hard to test in instrumentation without real user interaction.
- **Mitigation:** Rely on `FakeCameraPermissionGuard` to inject the state rather than trying to script the system dialog twice. Focus the test on the *app's response* to the state (opening settings), not the *system's transition* into it.
- **Risk:** Per-class threshold tuning might lead to "threshold sprawl" in the JSON.
- **Mitigation:** Keep the map small — only tune the 2/18 species currently in the AIY vocabulary. Use the global default for everything else.

---

## §6 Acceptance Criteria

- `docs/ROADMAP.md` is present and accurately reflects the project state.
- `TestIdentifyModule` is deleted; all tests use `@BindValue` or concrete injection.
- `LowConfidencePickerScreen` has a "Search" and "Manual Pick" affordance.
- Failures on `CameraScreen` trigger a Snackbar/Banner, not just a text overlay.
- `OnDeviceModelRealInterpreterTest` passes with a real photo and an accuracy assertion.
- `PermissionDeniedFlowTest` is enabled (no `@Ignore`) and GREEN.
- `LowConfidenceFlowTest` exists and is GREEN.
- All pre-PR gates (`./gradlew ... verifyNoNetworking` + `integration-flow.ps1`) are GREEN.
