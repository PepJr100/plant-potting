#!/usr/bin/env python3
"""
PLANTPOTTING-0007 — convert the House Plant Species MobileNetV2 model (.h5) to TFLite.

OFFLINE acquisition/conversion tool. It is NEVER invoked from a Gradle task and does NOT
run on device — it is a one-time developer step that produces the `model.tflite` asset.
`verifyNoNetworking` is therefore unaffected (no app runtime dependency is added).

The upstream model (Vatsalyakrish02/House_plant_species, Apache-2.0) is a TF-Hub MobileNetV2
feature-vector module + dense head over 47 house-plant classes. Input is 224x224 RGB,
normalized x/255 -> [0,1] (see the upstream app.py).

Why this runs on YOUR machine, not in the agent sandbox: it needs TensorFlow +
tensorflow_hub, which require a Python/TF toolchain the agent sandbox does not have.

------------------------------------------------------------------------------------------
USAGE
------------------------------------------------------------------------------------------
1. Create a clean env (TF 2.15-2.18 recommended; matches the upstream Keras/TF-Hub model):

     python -m venv venv && . venv/Scripts/activate      # Windows
     pip install "tensorflow==2.18.*" "tensorflow-hub" "numpy<2.1" pillow

2. Download the upstream weights:
     model/20240921-2014-full-image-set-mobilenetv2-Adam.h5
   from https://github.com/Vatsalyakrish02/House_plant_species (the `model/` dir).

3. Convert. DEFAULT = float16 (FLOAT32 input tensor, ~half size, fits the app's FLOAT32 branch):

     python convert_house_plant_model.py \
         --h5  path/to/20240921-2014-full-image-set-mobilenetv2-Adam.h5 \
         --out house_plant_species_mobilenetv2/model.tflite \
         --quant float16

   Alternatives:
     --quant none     # plain float32 TFLite (largest, most faithful)
     --quant int8     # full-integer; smallest (~3-4 MB) but needs --rep-dir of sample images.
                      # int8 makes the INPUT tensor uint8 -> use the UINT8 manifest variant
                      # (input_dtype="uint8", delete the normalization stanza). See ACQUISITION.md.

   For int8, supply a representative set (a few hundred images from the Kaggle 47-class set):
     python convert_house_plant_model.py --h5 ... --out ... --quant int8 --rep-dir path/to/sample_images

4. The script PRINTS the values you must paste into model_manifest.json:
     sha256, output_tensor_shape, label_count, input/output tensor dtypes, file size.
   Then follow ACQUISITION.md to move the bundle into app/src/main/assets/ml/ and flip the flag.
------------------------------------------------------------------------------------------
"""
import argparse
import glob
import hashlib
import os
import sys


def log(msg):
    print(msg, file=sys.stderr, flush=True)


def load_keras_model(h5_path):
    import tensorflow as tf  # noqa: F401
    import tensorflow_hub as hub
    from tensorflow.keras.models import load_model

    log(f"Loading Keras model: {h5_path}")
    # The upstream model embeds a hub.KerasLayer; it must be passed as a custom object.
    return load_model(h5_path, custom_objects={"KerasLayer": hub.KerasLayer})


def representative_dataset_gen(rep_dir, input_size=224, limit=300):
    """Yields preprocessed [1,224,224,3] float32 batches in [0,1] for int8 calibration."""
    import numpy as np
    from PIL import Image

    paths = []
    for ext in ("*.jpg", "*.jpeg", "*.png", "*.JPG", "*.JPEG", "*.PNG"):
        paths.extend(glob.glob(os.path.join(rep_dir, "**", ext), recursive=True))
    if not paths:
        raise SystemExit(f"--rep-dir {rep_dir} contained no images for int8 calibration")
    paths = paths[:limit]
    log(f"int8 calibration over {len(paths)} representative images")

    def gen():
        for p in paths:
            try:
                img = Image.open(p).convert("RGB").resize((input_size, input_size))
            except Exception:  # skip unreadable images
                continue
            arr = (np.asarray(img, dtype="float32") / 255.0)[None, ...]
            yield [arr]

    return gen


def convert(model, quant, rep_dir):
    import tensorflow as tf

    conv = tf.lite.TFLiteConverter.from_keras_model(model)
    if quant == "none":
        pass
    elif quant == "float16":
        conv.optimizations = [tf.lite.Optimize.DEFAULT]
        conv.target_spec.supported_types = [tf.float16]
    elif quant == "int8":
        if not rep_dir:
            raise SystemExit("--quant int8 requires --rep-dir with sample images")
        conv.optimizations = [tf.lite.Optimize.DEFAULT]
        conv.representative_dataset = representative_dataset_gen(rep_dir)
        conv.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
        conv.inference_input_type = tf.uint8   # UINT8 I/O -> use the app's UINT8 manifest variant
        conv.inference_output_type = tf.uint8
    else:
        raise SystemExit(f"unknown --quant {quant}")
    log(f"Converting (quant={quant}) ...")
    return conv.convert()


def report(tflite_path, tflite_bytes):
    """Print the manifest-fill values by loading the produced model in the TFLite interpreter."""
    import tensorflow as tf

    sha = hashlib.sha256(tflite_bytes).hexdigest()
    size = len(tflite_bytes)
    interp = tf.lite.Interpreter(model_content=tflite_bytes)
    interp.allocate_tensors()
    inp = interp.get_input_details()[0]
    out = interp.get_output_details()[0]

    print("=" * 70)
    print("CONVERSION COMPLETE — paste these into model_manifest.json")
    print("=" * 70)
    print(f"  file:                {tflite_path}")
    print(f"  size_bytes:          {size}  (~{size/1_000_000:.2f} MB)")
    print(f"  sha256:              {sha}")
    print(f"  input  tensor shape: {list(inp['shape'])}  dtype: {inp['dtype'].__name__}")
    print(f"  output tensor shape: {list(out['shape'])}  dtype: {out['dtype'].__name__}")
    print(f"  -> output_tensor_shape: {list(out['shape'])}")
    print(f"  -> label_count:         {list(out['shape'])[-1]}  (must equal 47)")
    print(f"  -> input_dtype:         "
          f"{'uint8 (delete normalization stanza)' if inp['dtype'].__name__=='uint8' else 'float32 (keep normalization mean=0,std=255)'}")
    print("=" * 70)
    if list(out["shape"])[-1] != 47:
        log("WARNING: output label count != 47 — labels.csv alignment must be re-checked!")


def main():
    ap = argparse.ArgumentParser(description="Convert House Plant Species MobileNetV2 .h5 -> TFLite")
    ap.add_argument("--h5", required=True, help="path to the upstream .h5 model")
    ap.add_argument("--out", required=True, help="output .tflite path")
    ap.add_argument("--quant", default="float16", choices=["none", "float16", "int8"])
    ap.add_argument("--rep-dir", default=None, help="dir of sample images for int8 calibration")
    args = ap.parse_args()

    model = load_keras_model(args.h5)
    tflite_bytes = convert(model, args.quant, args.rep_dir)
    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with open(args.out, "wb") as f:
        f.write(tflite_bytes)
    report(args.out, tflite_bytes)


if __name__ == "__main__":
    main()
