# Critique of PLANTPOTTING-0010 Drafts

## Draft A: Codex (`PLANTPOTTING-0010-CODEX.md`)

**What is stronger than mine:**
- **Persistence Technology:** Codex rightly identifies that Android Room is over-engineered for this sprint's data shape. It proposes `DataStore` with `kotlinx.serialization` ("The storage implementation can be a single DataStore file encoded as JSON"), which is lighter and meets the local-only requirement cleanly.
- **Seam Preservation Detail:** Codex is specific about *how* to preserve the `IdentificationResult` contract without polluting it. It explicitly tasks extending `NavCommand.Success` and `Routes.RESULT` to carry an optional `confidencePct`.
- **Baseline Phase:** Codex smartly includes "Phase 0 — Baseline and Guards" to run `./gradlew verifyNoNetworking` and record the APK size *before* starting work, establishing a clear baseline for the image size budget.

**What is weaker than mine:**
- **Theme Delivery UI:** Codex suggests a "compact segmented control" for the theme picker, whereas my draft specifies using a temporary debug menu/settings sheet or long-press on camera, which keeps the debug UI further out of the standard user paths.

**What tasks are missing:**
- Codex lacks a task to cap or limit the size of the saved plants collection in the local store. 
- It mandates saving a "created timestamp" but lacks a task to inject a time provider or clock for testing idempotence and ordering.

**What risks are underweighted:**
- Unbounded growth of the JSON DataStore file is not highlighted as a risk, which could degrade load times if a user scans hundreds of plants over time.

**What sequencing is wrong:**
- Codex places "Phase 6 — Reference Images and Licensing" very late in the sprint. Image hunting and sizing are high-risk for the APK budget; leaving this until Phase 6 risks discovering size overages or missing CC0 assets right before delivery.

## Draft B: Claude (`PLANTPOTTING-0010-CLAUDE.md`)

**What is stronger than mine:**
- **Architecture and Testing Rigor:** Claude's approach to the `DataStore` implementation is bulletproof. It explicitly lists tasks to "Implement an injectable time provider" for testability and to "Bound `identifiedPlants` length (most-recent-N cap)" to prevent unbounded growth.
- **Margin Confidence Nuance:** Claude deeply understands the unmapped candidate gap. It specifically tasks extending `MappedScore` with `topLabel`, `topProbability`, and `topIsMapped` to safely expose the strong unmapped predictions to `CameraViewModel` without touching the frozen seam.
- **Licensing Rigor:** Instead of my loose "add attribution" task, Claude mandates a strict `docs/licenses/reference-images.md` manifest and tasks adding a test to cross-check that every bundled image is recorded in the manifest.

**What is weaker than mine:**
- **Task Phasing Complexity:** Claude proposes a complex parallel branching execution flow (Phase 0 splitting into Phase 3 and Phase 4, Phase 1 in parallel). For a single developer, a straight sequential execution plan is less prone to local merge conflicts and context switching.

**What tasks are missing:**
- Claude lacks a concrete Phase 0 task to record the baseline APK size before adding reference images, even though its acceptance criteria correctly requires staying "within the recorded budget".

**What risks are underweighted:**
- Claude flags the "persistence built twice" risk but underweights the risk that relying heavily on complex new `MappedScore` flags could inadvertently swallow genuine low-confidence weak predictions if the routing threshold logic is slightly off.

**What sequencing is wrong:**
- Claude puts "Phase 5 — Pillar A1: theme candidates" near the end of the work. If the principal needs to review theme screenshots, generating the themes should happen earlier in the UI refresh so the principal has time to provide feedback while the rest of the sprint is coded.

## Merge Recommendation

If I were merging, I'd keep X from draft A and Y from draft B:
- **X from Draft A (Codex):** The strict Phase 0 Baseline execution (recording APK size, verifying no networking up front) and the specific `NavCommand.Success(confidencePct)` navigation argument extensions.
- **Y from Draft B (Claude):** The lightweight `DataStore` architecture with an injectable `Clock` and bounded size limit, along with the strict `docs/licenses/reference-images.md` manifest and its corresponding test cross-check.
