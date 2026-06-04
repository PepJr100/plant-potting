# PLANTPOTTING-0007 — House Plant Species MobileNetV2: acquisition & install

This is the **one developer step the agent sandbox can't do** (no TensorFlow toolchain): convert
the upstream `.h5` to a bundled `model.tflite`, then drop the completed bundle into the app and
flip the `ACTIVE_MODEL_ROOT` flag. Everything else (mapping, manifest, harness, wiring, fixtures,
JVM tests) is already authored.

## Net-free guarantee
The conversion script lives under `docs/sprints/evidence/` and is **never** called from Gradle or
on device. Downloading the upstream weights is a manual, one-time developer action — it adds **no**
app runtime dependency, so `verifyNoNetworking` stays GREEN. (Re-run it after you copy the bundle
in, as the plan's Phase 3 net-free gate requires.)

## Steps

1. **Download** the upstream model from
   [Vatsalyakrish02/House_plant_species](https://github.com/Vatsalyakrish02/House_plant_species),
   file `model/20240921-2014-full-image-set-mobilenetv2-Adam.h5` (~22.5 MB).

2. **Convert** (default float16 — FLOAT32 input tensor, fits the app's existing FLOAT32 branch):
   ```bash
   python -m venv venv && . venv/Scripts/activate
   pip install "tensorflow==2.18.*" tensorflow-hub "numpy<2.1" pillow
   python convert_house_plant_model.py \
       --h5 /path/to/20240921-2014-full-image-set-mobilenetv2-Adam.h5 \
       --out house_plant_species_mobilenetv2/model.tflite \
       --quant float16
   ```
   The script prints `sha256`, `output_tensor_shape`, `label_count`, input/output dtype, and size.
   - **Confirm `label_count == 47`** (must match `labels.csv`). If not, stop — the label alignment
     is wrong.
   - Want the smallest model (~3–4 MB)? Use `--quant int8 --rep-dir <dir-of-sample-images>` with a
     few hundred images from the [Kaggle 47-class set](https://www.kaggle.com/datasets/kacpergregorowicz/house-plant-species).
     **int8 makes the input tensor `uint8`** → use the UINT8 manifest variant (next step).

3. **Finish the manifest.** Copy `house_plant_species_mobilenetv2/model_manifest.template.json` to
   `model_manifest.json` and fill in from the script output:
   - `sha256`, `acquisition_date`, `output_tensor_shape`, `label_count`
   - set `placeholder` → `false`
   - **float16/none (default):** keep `input_dtype: "float32"` and the `normalization` stanza
     (mean=0, std=255 → reproduces `x/255`).
   - **int8 only:** set `input_dtype: "uint8"` and **delete** the `normalization` stanza (the
     quantization params encode it, exactly like AIY).

4. **Install the bundle** — copy the completed directory into the APK assets:
   ```
   docs/sprints/evidence/PLANTPOTTING-0007/house_plant_species_mobilenetv2/
       model.tflite                 (your converted file)
       labels.csv                   (already authored)
       plant_class_map.json         (already authored)
       model_manifest.json          (from step 3)
       LICENSE-house-plant-species.txt
   →  app/src/main/assets/ml/house_plant_species_mobilenetv2/
   ```

5. **Probe it (Phase 3).** Run the swap-eval harness on the emulator to get the candidate's
   per-fixture numbers (the bundle is auto-picked up by `ModelSwapEvaluationTest`):
   ```
   ./gradlew :app:pixel6Api34DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.identify.ModelSwapEvaluationTest
   ```
   Paste `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval.csv` back so Phase 4 can
   re-baseline thresholds from real fixture scores.

6. **Flip the prototype flag (Phase 5)** once the probe confirms it beats AIY:
   in `app/build.gradle.kts`, set the `ACTIVE_MODEL_ROOT` BuildConfig default (or the prototype
   build flavor) to `ml/house_plant_species_mobilenetv2`. Default stays `ml/aiy_plants_v1`.

7. **Live smoke run (G4).** Run `OnDeviceModelAppWiredPrototypeTest` (Phase 5) on `pixel6Api34`
   with the flag flipped, and capture logcat/screenshot evidence that ≥3 houseplant fixtures
   identify directly through `OnDevicePlantIdentifier` with `source == ON_DEVICE_MODEL`.

## If conversion fails
MobileNetV2 + TF-Hub feature vectors are standard TFLite-supported ops, so a clean conversion is
expected. If you hit an unsupported-op or a `hub.KerasLayer` deserialization error, try TF 2.15–2.18
and matching `tensorflow-hub`; if it persists, record it under this directory as a conversion
blocker — the fallback is AIY stays the default and the swap *mechanism* is still demonstrated.
