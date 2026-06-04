# PlantPotting — Build & Sprint Workflow

How work moves through this project: the skill-driven sprint cycle, the gates each phase
must clear, and the merge-gated handoffs between phases. For day-to-day setup and
commands, see [`docs/runbook.md`](runbook.md). For end-user behaviour, see
[`docs/userguide.md`](userguide.md).

## The sprint cycle

Work moves through a repeating cycle, each step a `/slash-command` skill, with the sprint
ledger (`docs/sprints/ledger.yaml`) as the source of truth for which sprint is in which
state:

```
roadmap ──▶ sprint-planner ──▶ sprint-execute ──▶ sprint-review ──▶ (roadmap bump)
(the long     (3 models draft,    (1 model builds    (human exercises    ▲
 narrative)    critique, merge)     it, TDD-gated)     the real app, logs) │
    └──────────────────────────────────────────────────────────────────┘
```

- **`/roadmap`** — maintains [`docs/ROADMAP.md`](ROADMAP.md) (INIT / REFRESH / BUMP). The
  long narrative the rest of the cycle writes against.
- **`/sprint-planner`** — multi-model planning. codex, gemini, and claude each draft
  independently, cross-critique, then Opus merges the strongest plan into
  `docs/sprints/{SID}.md`.
- **`/sprint-execute`** — single-model implementation (opus / codex / gemini) under a
  strict TDD brief; flips `- [ ]` → `- [x]` in the plan as each task lands.
- **`/sprint-review`** — human walks the real app, logs structured feedback to
  `docs/sprints/feedback/{SID}/feedback.md`, which feeds the next plan.

The skills live in `.claude/skills/` and are **gitignored** (local-only) — a fresh clone
won't have them; install them before `/sprint-*` works. See `.claude/CLAUDE.md`.

## Merge-gated `origin/main` handoffs

Each seam hands off through `origin/main` so no phase can quietly lose a plan or a feedback
file on a feature branch:

- **planner** refuses to start until the *previous* sprint is `done` and its `feedback.md`
  is on `origin/main`.
- **execute** refuses to dispatch until the plan `{SID}.md` is on `origin/main`.
- **review** refuses to start until the execution's checkbox flips are on `origin/main`.

Work happens on `sprint/{SID}` (and `roadmap/*`) branches; each phase opens a PR with
`--base main`. Don't stack a review PR on an execution branch — re-target to `main` if you
do (`gh pr edit <N> --base main`).

## Gates (what "done" must clear)

These run locally and in CI. The full chain is in [`docs/runbook.md`](runbook.md#full-pre-pr-check):

| Gate | Command | Asserts |
| --- | --- | --- |
| Unit tests | `./gradlew.bat testDebugUnitTest` | KB validator, recommendation engine, ViewModels, Robolectric Compose UI, model contract tests. |
| Instrumentation (GMD) | `./gradlew.bat pixel6Api34DebugAndroidTest` | What CI runs; boots a headless AOSP Pixel 6 / API 34. Includes `OnDeviceModelRealInterpreterTest` (real `.tflite` end-to-end). |
| Lint + style | `./gradlew.bat lint ktlintCheck` | Android lint + ktlint. |
| Network-free | `./gradlew.bat verifyNoNetworking` | Inference stays on-device; no networking is linked in. |
| Stub isolation | `bash scripts/check-stub-isolation.sh` | `StubPlantIdentifier` is referenced only from `identify/`, never leaks into production wiring. |
| Integration manifest | `pwsh ./scripts/integration-flow.ps1` (+ `-BuildOnly`) | Drives the full camera→result→recipe flow on the emulator, diffs an integration manifest against `docs/sprints/expected-artifacts/`. |

## TDD brief (every implementer)

`/sprint-execute` hands every implementer the same rules:

1. **Failing test first**, then implement.
2. **Integration tests are mandatory for feature tasks** — a feature isn't done until the
   integration script exercises it through the live app/emulator with real data
   verification (the source badge, the recipe-row count, the routing decision — not just
   "a screen rendered").
3. **Never check off an integration/acceptance task** without an actual diff to the test
   script.

## Artifacts on disk

- `docs/sprints/{SID}.md` — the merged plan (the contract: tasks, non-goals, risks,
  acceptance criteria).
- `docs/sprints/drafts/{SID}-*.md` — per-model drafts + critiques (how the plan was
  derived).
- `docs/sprints/results/{SID}.md` — per-sprint state snapshot.
- `docs/sprints/feedback/{SID}/feedback.md` — review feedback that feeds the next plan.
- `docs/sprints/evidence/{SID}/` — transcripts / probe data backing acceptance claims.
- `docs/sprints/ledger.yaml` — per-sprint status + executor; the source of truth.
