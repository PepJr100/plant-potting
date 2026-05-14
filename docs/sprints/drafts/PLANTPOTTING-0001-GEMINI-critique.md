# Critique of Sprint Drafts: PLANTPOTTING-0001

**Critique Author:** GEMINI
**Target Drafts:** 
- `docs/sprints/drafts/PLANTPOTTING-0001-CODEX.md`
- `docs/sprints/drafts/PLANTPOTTING-0001-CLAUDE.md`

---

## 1. Critique of CODEX Draft

### 1.1 Strengths
- **Verification Automation:** The inclusion of `scripts/check-android.ps1` and `scripts/integration-flow.ps1` is superior to my own draft. It treats the "runnable" requirement as something to be proved by code, not just manual observation.
- **KB Rigor:** CODEX specifies exact validation rules (e.g., "recipe proportions total 100", "rationale is exactly one sentence") in Phase 2. This prevents "plausible prose" from drifting into untestable garbage.
- **CI Readiness:** Explicitly listing `adb pm grant` for automation shows a deeper understanding of the "greenfield Android" risk where permission dialogs often break headless tests.

### 1.2 Weaknesses
- **Vague UI Layout:** Task "Implement home screen with app title..." is generic. It lacks the specific UI components (like ingredients tables or rationale placement) that CLAUDE and I both defined more clearly.
- **Missing Navigation Context:** It doesn't explicitly mention the "Retake" or "Back to Camera" flow which is critical for an E2E stub intended for demos.
- **Phase 0 Overhead:** While the research extraction is good, it feels like "work about work" for a single AI implementer who should already have these docs in context. It risks burning 10% of the sprint on documentation that already exists.

### 1.3 Missing Tasks
- **Error States:** Missing specific tasks for handling "Camera Hardware Not Found" or "Storage Full" scenarios during capture.
- **UI Testing for KB:** It lacks a task for a "Golden Test" or similar to ensure the UI actually renders all 16 species correctly without clipping or layout breaks.

### 1.4 Risks Underweighted/Missing
- **Emulator Flakiness:** CODEX relies heavily on `scripts/integration-flow.ps1` but doesn't address the high failure rate of CameraX on generic emulators without specific hardware acceleration.

### 1.5 Sequencing Issues
- **Phase 5 Congestion:** Putting CameraX, E2E validation, and script creation all in Phase 5 is a bottleneck. If CameraX breaks (and it will), the entire validation phase stalls.

---

## 2. Critique of CLAUDE Draft

### 2.1 Strengths
- **Sophisticated KB Schema:** The `ArchetypeMapping.Blend` support (Phase 1.1) is a major upgrade. The `Hoya carnosa` example proves the engine can handle complexity early, which is better than my "Single-mapping only" approach.
- **Rationale Templates:** Using a `{species}` placeholder in `rationaleTemplate` (Phase 1.2) is a much cleaner way to generate professional-sounding recommendations than hard-coding strings for every species.
- **"Golden" Testing:** `2.4 RecommendationGoldenTest` is excellent. It ensures that even if the JSON is valid, the engine produces sensible results for the *entire* production KB.

### 2.2 Weaknesses
- **Complexity Bloat:** 60+ checkboxes for a 2-week sprint is dangerously aggressive for a single AI. Tasks like "8.1-8.4 (Discipline tasks)" and "0.5 (Detekt/Ktlint)" are valuable but might be "gold-plating" for a bootstrap sprint.
- **Physical Device Hard-Requirement:** §7.5 requires a physical device. For an autonomous AI agent, this is a "blocked" task unless a cloud farm is provided. It's an unrealistic "Done" criterion for this specific implementer.
- **Over-engineered Loader:** Phase 1.4-1.8 describes a very robust `KbLoader`. For 16 species, this could have been a simple Kotlin Object or a much lighter JSON parse.

### 2.3 Missing Tasks
- **Navigation Hardware Interaction:** No mention of handling the "System Back" button explicitly in Compose (though §1.1 mentions the gesture).
- **Temporary File Cleanup:** Capturing images as JPEGs (§5.3) will fill up the cache; there's no task to manage or purge these temporary files.

### 2.4 Risks Underweighted/Missing
- **Hilt Overhead:** CLAUDE identifies Hilt as a risk but still plans for complex Hilt test rules. In a 2-week sprint, Hilt setup can often eat 2 days of "Dagger-hell" if the dependency graph gets circular.

### 2.5 Sequencing Issues
- **Parallel Discipline (Phase 8):** While marked as parallel, these tasks often break the build (lint failures) and can frustrate progress on core features if not integrated earlier.

---

## 3. Comparison Summary

| Feature | GEMINI | CODEX | CLAUDE |
| :--- | :--- | :--- | :--- |
| **KB Quality** | Basic (Single) | High (Validation) | Elite (Blends/Templates) |
| **Testing** | TDD Focus | Integration Scripts | Golden/Schema Tests |
| **Automation** | Local Only | Script-Heavy | CI/GMD Heavy |
| **Simplicity** | High (Safest) | Medium | Low (Aggressive) |

---

## ## If I were merging

- **Keep from CODEX:** The automation scripts `scripts/check-android.ps1` and `scripts/integration-flow.ps1`.
- **Keep from CODEX:** The `adb pm grant` approach for bypassing permission flakiness in tests.
- **Keep from CLAUDE:** The `ArchetypeMapping.Blend` logic and the `{species}` rationale templates.
- **Keep from CLAUDE:** `RecommendationGoldenTest` to ensure KB integrity against the engine.
- **Keep from GEMINI:** The "Retake" button and simple "Camera-first" navigation to maximize demo value.
- **Drop from CODEX:** The Phase 0 "Research Extraction" tasks (redundant for an AI with the docs in context).
- **Drop from CLAUDE:** The physical device requirement (replace with Gradle Managed Devices).
- **Drop from CLAUDE:** The `Detekt`/`Ktlint` setup (defer to a "Polish" sprint to save time now).
- **Drop from GEMINI:** The "MVI/MVVM" indecision; commit to CLAUDE's `*UiState` pattern.
- **Merge Point:** Use CLAUDE's 16-species list as the definitive "First 16" for the KB.
