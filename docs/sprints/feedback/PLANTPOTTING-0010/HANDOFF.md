# PLANTPOTTING-0010 — execution → review handoff

**Status:** all plan checkboxes ticked + a substantial round of principal review-feedback folded in
(7 on-device iterations). Implementer: **opus (claude-opus-4-8)**, in-session. Branch
`exec/PLANTPOTTING-0010` (local until pushed). App version **v0.4.0** (versionCode 4).

## APK to test

Latest debug build in Dropbox:
`C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\app-debug-PLANTPOTTING-0010-v0.4.0-r7-home-buttons.apk`
(earlier r1–r6 are intermediate review rounds; **r7 is current**).

## Gate status (all GREEN, host JVM)

`:app:testDebugUnitTest` · `verifyNoNetworking` · `scripts/check-stub-isolation.sh` ·
`:app:lintDebug` (abortOnError) · `:app:assembleDebug` · `:app:compileDebugAndroidTestKotlin`.
**GMD `pixel6Api34` was NOT run locally** (no emulator in this environment) — instrumented sources
compile clean; run them in CI/review. APK delta vs Phase-0 baseline ≈ **+2.2 MiB** (budget ≤3–5 MiB;
reference images ≈ 1.5 MiB).

## What shipped (vs the plan)

All 8 phases landed (DataStore persistence; +12 KB species → **38/47 mapped, 44 species**; confidence
%+bar; contained search; My Plants; "Add this plant"; reference images; themes; version bump). On top
of the plan, the principal's review drove:

- **Home/landing screen** (Dribbble-concept layout: greeting + 2×2 tiles + recent-plants carousel).
  The app now *starts* here.
- **Full-width green "Home" button** at the **bottom of every screen** (shared `HomeButton`).
- **My Plants**: de-dup by species (no duplicate saves) + per-row **remove**; rows carry a photo
  thumbnail; the redundant "on-device match" badge was removed there.
- **Potting-mix page** reordered: plant name (carded) → picture → description (carded) → recommended
  mix (carded) → recipe; **Retake replaced by Home**.
- **34 → 44 CC0/PD reference photos** (every species) sourced from Wikimedia Commons via
  `scripts/source-reference-images.ps1` + `source-missing-images.ps1` (license-filtered to CC0/PD).
- **Removed** the camera three-dot menu icon and the debug **Theme switcher** (entry, route, screen,
  VM) at the principal's request. Theme infra (LEAF/TERRACOTTA/SLATE candidates + persistence) is
  retained but unreachable from the UI; production default is **LEAF**.

## What to exercise in review (acceptance)

1. **Home** lands first; tiles route correctly; recent carousel shows saved plants.
2. **Identify → confidence**: a confident mapped capture shows the Result reference photo + **% and
   bar**; degrades gracefully (no %/bar) on stub flows.
3. **Contained search** in the low-confidence picker (list revealed on focus/typing).
4. **Add this plant**: a confident-but-**unmapped** class (Pilea is the live fixture) routes to the
   wireframe; tapping logs a request; "Pick manually" falls through; a mapped-high class still →
   Result; weak → picker.
5. **My Plants**: explicit Save; **survives app restart** (close + reopen); **no duplicates**;
   **remove** works; thumbnails render.
6. **Reference photos** render on Result / Recommendation / Home carousel / My Plants; missing-image
   species show the placeholder without crashing.
7. **Potting-mix** order + **Home button** at the bottom of every screen.
8. **Pilea stays unmapped** (CI guard); new mappings are *editorial coverage, not calibrated*.

## Known limitations / notes for the reviewer

- **Reference imagery is CC0/PD only.** A few are vintage botanical *plates* rather than photos where
  that was the only clean option (e.g. peace lily, poinsettia, parlor palm, dracaena, and
  `philodendron-pink-princess`, whose PD image is of the *species* `Philodendron erubescens`, not the
  cultivar). Full provenance: `sourced-images.tsv`; attribution: `docs/licenses/reference-images.md`.
  To swap any out or allow the broader "free-to-use" (Unsplash/Pexels, non-PD) bucket, re-run the
  scripts after editing the license filter.
- **Theme candidates** are no longer switchable in-app. If the principal wants a non-LEAF default,
  it's a one-line change in `MainActivity`/the persisted default — flag it and it's quick.
- The **mapped delta species (0009 + 0010) are uncalibrated** against real photos (editorial coverage)
  — same standing gap as 0009.
- The plan's "per-candidate theme screenshots" deliverable is **obsolete** (switcher removed).

## Doc currency

Updated this handoff: `README.md` (current-state + architecture), `docs/userguide.md` (Home, My Plants,
Add this plant, confidence, photos), `docs/kb/ml-mapping-notes.md` (§PLANTPOTTING-0010),
`docs/licenses/reference-images.md`. **`docs/ROADMAP.md` is still at `through_sid: 0009`** — bump it via
the `/roadmap` skill (out of scope for execute).

## Merge gate reminder

Execution commits are local on `exec/PLANTPOTTING-0010`. **Push the branch and open a PR against
`main`** — `/sprint-review` refuses to start until this execution PR is merged.
