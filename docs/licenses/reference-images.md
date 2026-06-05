# Reference image attribution manifest

PLANTPOTTING-0010 Phase 6 / D4. Every bundled reference image under
`app/src/main/res/drawable-nodpi/*.webp` **must** have a row in the table below, and every image must
be **CC0 or public-domain** (PD). This is CI-enforced by
`ReferenceImageManifestTest.everyBundledReferenceImageHasAManifestEntry` — adding a WebP without a
manifest row reddens the build.

## License policy (D4)

- **CC0 / public-domain only.** Sprint 0008 found CC-BY-NC dominates houseplant imagery and is
  unusable; even CC-BY adds a per-image attribution burden, so we restrict to CC0/PD. Good sources:
  Wikimedia Commons files explicitly tagged CC0/PD, and pre-1929 botanical plates (e.g. Köhler's
  *Medizinal-Pflanzen*).
- **No self-shot / first-party plant imagery** (sprint non-goal).
- **Format/budget:** downscaled **WebP** (~40 KB/image), rendered with `painterResource` (no image
  library / Coil — keeps the app network-free). Total growth budget **≤ 3–5 MiB** over the Phase-0
  baseline debug APK (43,479,722 bytes / ~41.46 MiB).

## Current coverage

**No CC0/PD photos are bundled yet** — per D4 we ship the resolver + an authored placeholder vector
(`res/drawable/ic_plant_placeholder.xml`, no external license) and add verified images
incrementally rather than block the sprint on scarce license-clean imagery. Every species currently
renders the placeholder on `ResultScreen` (theme-tinted). The APK delta for Phase 6 is therefore
~0 (one vector), well inside budget.

## How to add a verified image

1. Confirm the source license is **CC0 or PD** (record the exact license + source URL).
2. Downscale/compress to ~display size as **WebP** (~40 KB) and place it at
   `app/src/main/res/drawable-nodpi/<name>.webp`.
3. Register it in `PlantImageResolver.images` (`speciesId → R.drawable.<name>`).
4. Add a row to the table below.
5. Re-run `./gradlew :app:testDebugUnitTest` (the manifest cross-check + APK-size budget guard).

## Manifest

| Image file (`drawable-nodpi/`) | KB speciesId | Source URL | License |
|---|---|---|---|
| _(none yet — placeholder only)_ | — | — | — |
