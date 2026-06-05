# PLANTPOTTING-0010 — Refined Intent (brief for drafters)

PLANTPOTTING-0010 is a combined **"app-experience" sprint** with three pillars. It deliberately
diverges from the roadmap's planned Next (the pothos↔Pilea boundary fix), which **stays deferred**.

## Hard constraints (apply to every pillar)
- Behind the **frozen `PlantIdentifier` / `IdentificationResult` seam** — do not change the
  identification contract. (`app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt`)
- **Network-free**: the `verifyNoNetworking` gradle gate and `scripts/check-stub-isolation.sh` must
  stay GREEN. All assets (themes, reference photos) are **bundled**, not fetched.
- **No model training** and **no first-party / self-shot plant imagery** (hard user constraint).
  NOTE: app-UI screenshots of candidate themes are fine — those are renders, not plant training data.
- Reference photos + theme assets must be **license-clean (e.g. CC/public-domain)** and bundled →
  watch APK size and attribution/licensing.
- Bump the app version so a testable debug APK can be delivered to the user's Dropbox.

## Pillar A — UI/UX refresh (idea inbox #2)
1. **Theme picker deliverable:** build **2–3 candidate Compose themes** (clean/elegant aesthetic;
   user ref: a Dribbble plant-app concept) behind a switch, build the app, and deliver
   **screenshots + a debug APK** so the principal picks one from real renders. Do NOT silently
   hardcode a single theme — produce pickable candidates. Current theme lives in
   `app/src/main/java/com/darkfactory/plantpotting/ui/theme/{Color.kt,Theme.kt}` and
   `app/src/main/res/values/themes.xml`.
2. **Confidence display:** show a numeric percentage **and** a progress bar on the match/confidence
   UI (Result / Recommendation screens).
3. **"My Plants" folder:** a list of previously identified plants. This requires the app's **first
   local-persistence layer** (DataStore or Room) — a new architectural element to design carefully.
4. **Search containment:** the species list currently rendered under the search-species box should be
   **contained within** the search-species control, not an always-visible separate list.
5. **Reference photo per plant:** show a license-clean reference image of each plant when you click
   through to it / view its potting-mix recipe.

## Pillar B — "Add this plant" wireframe (idea inbox #1)
- For **strong-confidence model matches with NO KB entry** (e.g. among the 21 unmapped model
  classes), surface the match and an **"Add this plant" button**. The button is a **wireframe for
  now** but must **locally log/total the requests** (local-only, network-free) so the principal can
  collect/total them later. Decide how this interacts with the existing low-confidence path.

## Pillar C — KB expansion (idea inbox #4 / option 4)
- Continue the **text-only KB expansion** over the **popular slice of the remaining 21 unmapped
  model classes** (cheap, no ML, no self-shot). KB assets:
  `app/src/main/assets/kb/species.json` (currently 32 species),
  `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` (26 of 47 mapped).
- **Pilea (`Chinese Money Plant`) stays deliberately unmapped** (CI-enforced Pilea-absence guard in
  `HousePlantClassMapValidationTest`) and the pothos↔Pilea boundary fix stays **deferred**.

## Cross-pillar concerns the plan must address
- **Shared persistence dependency:** "My Plants" (A3) and the "Add this plant" request log (B) both
  need local storage — design one persistence layer, not two.
- **Sequencing** of a heterogeneous sprint (app-shell UI vs. new feature vs. content).
- **Reference-image licensing + APK size** tension (A5) — may need to bound species count or use
  a placeholder strategy if license-clean images aren't available for all 32 species.
- Existing screens: `camera/CameraScreen.kt`, `result/{ResultScreen,RecommendationScreen,
  LowConfidencePickerScreen,ArchetypePickerScreen}.kt`, nav in `ui/navigation/`.
