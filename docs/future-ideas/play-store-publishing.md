# Publishing PlantPotting to the Google Play Store + source licensing

Companion write-up for the `## developed idea` entry in [`feature-ideas.md`](feature-ideas.md). Captures the licensing decision, the publishing process, Play's significant requirements, and a ready-to-run `/roadmap` fold-in prompt with content pre-mapped to ROADMAP sections.

**Principal decisions (locked 2026-06-09):** public GitHub repo under **PolyForm Noncommercial 1.0.0**; **Personal** Play developer account (so the 12-tester / 14-day closed-test gate applies); **free launch** (monetisation is a separate raw idea, deferred).

**Headline:** licensing is already commercial-clean — both bundled ML models are Apache-2.0, all 44 reference photos are Unsplash/Pexels/CC0, no GPL/copyleft, `verifyNoNetworking` holds. The work is a release-engineering + Play-process initiative — a new **Launch** track that runs alongside the V1 accuracy work, not blocking it.

> Android apps publish to the **Google Play Store** (the Apple App Store is iOS-only).

---

## Q1 — Licensing: PolyForm Noncommercial 1.0.0

**You already own copyright automatically** — from the moment the code was written (Berne Convention; no registration, GitHub, or Play Store needed). Publishing to Play does **not** create or transfer copyright; Google's Developer Distribution Agreement explicitly states you keep all your IP. The repo's current no-license state is technically "all rights reserved" (nobody may reuse it) but is a weak deterrent and signals nothing.

**What PolyForm Noncommercial does.** A short, plain-language **source-available** license (SPDX `PolyForm-Noncommercial-1.0.0`). Anyone may use/modify/redistribute — **but only for a "noncommercial purpose"** (*"Any noncommercial purpose is a permitted purpose"*: research, study, hobby/amateur projects, plus charities/schools/government). Redistributors must pass along the license terms and any `Required Notice:` lines. So a clone-and-monetise is a **license violation** — which removes the commercial incentive to copy.

**Ins (why it fits):** repo stays public as a portfolio piece; one short file; you keep every right — the license binds *licensees*, not you, so launching free now and monetising later (or dual-licensing commercially) stays open.

**Issues to accept:**
- **Not OSI "open source."** GitHub's picker won't badge it; some "OSS-only" users/orgs avoid it.
- **"Noncommercial" is genuinely fuzzy at the edges** — the most-cited criticism. Clear cases (selling a clone) are obvious; gray cases are arguable.
- **It does NOT by itself bar a Play Store clone** — no license can while the code is public. It makes cloning a *violation* you'd enforce via DMCA/takedown. Your stronger real-world levers are **(a)** Play's IP/clone-takedown process and **(b)** a **trademark on the app name** — neither depends on the code license.
- **Covers only YOUR code.** The bundled Apache-2.0 models and Unsplash/Pexels/CC0 photos keep their own terms (a copier could grab those from their original public sources anyway). Standard "aggregate" mix; keep the existing per-asset `LICENSE-*.txt` and `docs/licenses/reference-images.md`.
- **Contributions:** outside PRs would carry contributors' copyright under the same license — fine for solo; revisit with a CLA if collaborators appear.

**Setup:** root `LICENSE` with the full PolyForm Noncommercial 1.0.0 text + filled-in licensor line (name/handle + year); a short README licensing section; optionally a `Required Notice:` attribution line.

## Q2 — Publishing steps

1. **Create a Google Play Developer account** — one-time **$25**, Personal type; complete **identity verification** (government ID).
2. **Make the code release-ready** — the `PLANTPOTTING-0014` scope below.
3. **Generate an upload key + enroll in Play App Signing** (let Google manage the app-signing key).
4. **Build a signed release `.aab`** (Play no longer accepts APKs for new apps).
5. **Create the app in Play Console** — name, default language, free, app (not game).
6. **Complete store listing + policy declarations** (Q3).
7. **Run the closed test** — Personal accounts created after Nov 2023 need **≥12 testers opted in for 14 continuous days** before requesting production access. Recruit testers early — it's the schedule long pole.
8. **Request production access**, address review feedback, **roll out** (staged % rollout available).
9. The existing tag-triggered `release.yml` (debug APK to GitHub) can stay for side-loaders — separate from Play.

## Q3 — Significant requirements

- **Target API level: API 35 (Android 15) minimum** for new submissions (since 31 Aug 2025); **API 36 (Android 16) required from 31 Aug 2026.** The app is `targetSdk 34` — **below the current minimum** (hard blocker).
- **App Bundle (`.aab`)** required, signed via Play App Signing.
- **Privacy policy at a public URL** (the app holds `CAMERA`) — trivial given on-device-only design.
- **Data Safety form** — declare *no data collected/shared*.
- **Content rating** (IARC, likely Everyone); target audience; app access (all features open); ads declaration (none).
- **Store-listing assets:** 512×512 app icon, 1024×500 feature graphic, ≥2 phone screenshots, short + full description, category.
- **Closed-testing gate:** 12 testers × 14 continuous days (per Q2 step 7).

*Requirement facts current as of June 2026 web research (Play Console Help: testing requirements & target-API policy; developer-verification/$25-fee pages; PolyForm Noncommercial 1.0.0 license text). Play policies shift — confirm live numbers in Console at submission time.*

---

## Proposed sprint — PLANTPOTTING-0014 (release readiness)

Behind the frozen `PlantIdentifier` seam; no KB/model changes.

**Code/config:**
1. `LICENSE` = PolyForm Noncommercial 1.0.0 + README licensing section (keep per-asset licenses).
2. Bump `targetSdk`/`compileSdk` 34→35 (eval 36) + fix API-35 edge-to-edge / predictive-back regressions in Compose.
3. Release `signingConfig` + Play App Signing; keystore + secrets **out of the repo** (current no-keystore state is the safe baseline).
4. `bundleRelease` → signed AAB.
5. Enable R8 (`isMinifyEnabled = true`) + TFLite keep rules (`org.tensorflow.**`); **verify inference + `AccuracyEvalTest` on the minified release variant** (TFLite + reflection can break under R8). Ship minify-off for v1 if fragile (costs size + easier decompile only).
6. Real launcher icon (replace the placeholder `ic_launcher_foreground.xml`) + 512×512 Play icon.
7. `docs/PRIVACY.md` (on-device classification; camera only during ID; images not retained/transmitted; records local; no analytics) hosted at a public URL (GitHub Pages).
8. Version bump → **v1.0.0** (`versionCode 8`); keep gates green (`ktlintFormat` first per the Windows autocrlf note).

**Play process (plan-tracked, not code):** $25 Personal account + ID verification; Data Safety = none collected; content rating; store-listing assets; the 12-tester/14-day closed test (recruit early).

---

## `/roadmap` fold-in prompt

`docs/ROADMAP.md` is at `through_sid: PLANTPOTTING-0013` with 0 newer `done` sprints, so `/roadmap` auto-routes to a no-op. Path: seed the inbox (done — see `feature-ideas.md`), then run **`/roadmap refresh`** and accept the idea-inbox off-ramp. REFRESH is multi-model + goes through a feature branch + PR.

Prompt to hand the skill:

> We're folding a **new Launch track — publishing PlantPotting to the Google Play Store** — into the ROADMAP. This isn't a sprint-window reconciliation; it's a net-new direction from the idea inbox. Principal decisions: **public GitHub repo licensed PolyForm Noncommercial 1.0.0**; **Personal Play developer account**; **free launch, monetisation deferred**. Licensing is already commercial-clean (both bundled ML models Apache-2.0; all 44 reference photos Unsplash/Pexels/CC0; no GPL/copyleft; `verifyNoNetworking` holds). Update these sections (leave Current State / Sprint History / title untouched — mechanical):
>
> 1. **Known Gaps** — add a "not release-ready for Play Store" gap (the 5 blockers + 2 doc gaps below) with **Why it matters** + **Likely sprint** = `PLANTPOTTING-0014 (release readiness)`. Add a smaller "no project LICENSE / no privacy policy" gap. Keep the existing accuracy/Pilea gaps.
> 2. **Layer Status** — add two rows: **Licensing & legal** and **Release engineering / signing / distribution** (states below).
> 3. **Standing non-goals** — note the Launch sprint **lifts** the `targetSdk`/`compileSdk` 34→35 (eval 36) bump (the "AGP/Kotlin/Compose/Hilt/TFLite versions locked" non-goal yields for the Play target-API requirement) and a README **append** for a licensing section. All other locks (frozen seam, KB, no training) stay.
> 4. **Proposed Sprint Path** — add a new **Launch / Public release** milestone and a detailed **Next** block for `PLANTPOTTING-0014 — release readiness`. Frame Launch as parallel to V1 accuracy work, not gating it.
> 5. **Changelog** — append one entry recording the Launch-track fold-in.

### Content pre-mapped to ROADMAP sections

**→ Known Gaps (new):**
- **★ Not release-ready for the Play Store (blocks publication).** (1) `targetSdk`/`compileSdk` = 34 < Play minimum (API 35 now, 36 from 31 Aug 2026) — bump to 35 (eval 36) + fix edge-to-edge/back regressions; (2) no release signing — add `signingConfig` + Play App Signing, secrets out of repo; (3) only a debug APK — need a signed `.aab` via `bundleRelease`; (4) placeholder launcher icon — real adaptive + 512×512 Play icon; (5) R8 disabled — enable + TFLite keep rules, verify inference on a minified release build (ship off if fragile). *Likely sprint:* PLANTPOTTING-0014.
- **No project LICENSE and no privacy policy.** Repo is "all rights reserved" by default (weak deterrent); Play requires a privacy-policy URL (app holds `CAMERA`). Add `LICENSE` (PolyForm Noncommercial 1.0.0) + README section (keep per-asset licenses); author `docs/PRIVACY.md` at a public URL.

**→ Layer Status (new rows):**

| Layer | Status | Notes |
| --- | --- | --- |
| Licensing & legal | ⚠ partial | Bundled ML models Apache-2.0; 44 photos Unsplash/Pexels/CC0 (CC-BY-SA banned by policy); no GPL/network deps — **commercial-clean**. **Missing:** project `LICENSE` (PolyForm Noncommercial 1.0.0) + privacy policy. |
| Release engineering / signing / distribution | ✗ not started | `release.yml` emits a debug APK to GitHub on `v*` tags only. No release `signingConfig`, no Play App Signing, no `bundleRelease`/AAB, R8 off, placeholder launcher icon. Target SDK 34 < Play minimum 35. |

**→ Standing non-goals (lifts):** `targetSdk`/`compileSdk` 34→35 (eval 36) bump lifts the version-lock non-goal for the SDK target only; README licensing-section append (allowed). Frozen seam / KB lock / no training / no net-new screens unchanged.

**→ Proposed Sprint Path — new milestone:**
> #### Launch — Google Play Store public release
> - **Exit criteria:** signed release **AAB** of a min-API-35 build, PolyForm-Noncommercial-licensed public repo, privacy policy live, store listing + Data Safety + content rating complete, and the **12-tester / 14-continuous-day closed test** passed → production rollout.
> - **Runs parallel to V1** (accuracy) — release engineering + Play process, behind the frozen seam; does not block or depend on accuracy work.
> - **Skeleton sprints:** `PLANTPOTTING-0014` release readiness; then a `0014`-review + closed-test-management follow-up if the tester gate surfaces issues.

**→ Proposed Sprint Path — detailed Next block:** the `PLANTPOTTING-0014` scope above (code track + Play-process track).

**→ Changelog (append):**
> - <date> — folded in a new **Launch track — Google Play Store release** (from the idea inbox; not a sprint-window reconcile). Recorded the release-readiness gap (targetSdk 34 < Play min 35; no signing/AAB; placeholder icon; R8 off; no LICENSE/privacy policy) and the licensing decision (**public repo, PolyForm Noncommercial 1.0.0**; bundled assets already commercial-clean). Added a **Launch** milestone (parallel to V1) + a detailed `PLANTPOTTING-0014` Next block, two Layer-Status rows, and noted the targetSdk-bump lock lift. Principal decisions: Personal Play account (12-tester/14-day gate), free launch, monetisation deferred.
