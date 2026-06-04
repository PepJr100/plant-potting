#!/usr/bin/env python3
"""Sanity inference: run the converted TFLite over the androidTest fixtures and print
top-3 labels. Validates label-order alignment (labels.csv vs model output indices)
end-to-end. Not part of the app; offline dev check only."""
import csv
import sys
import numpy as np
from PIL import Image
import tensorflow as tf

MODEL = "house_plant_species_mobilenetv2/model.tflite"
LABELS = "house_plant_species_mobilenetv2/labels.csv"
FIXDIR = "../../../../app/src/androidTest/assets/identify-fixtures"

# fixture filename -> expected label substring
EXPECT = {
    "dracaena-trifasciata.jpg": "Snake plant",
    "epipremnum-aureum.jpg": "Pothos",
    "goeppertia-orbifolia.jpg": "Calathea",
    "phalaenopsis.jpg": "Orchid",
    "spathiphyllum-wallisii.jpg": "Peace lily",
    "zamioculcas-zamiifolia.jpg": "ZZ Plant",
}

labels = [r[0] for r in csv.reader(open(LABELS, encoding="utf-8")) if r]
print(f"labels: {len(labels)}")

interp = tf.lite.Interpreter(model_path=MODEL)
interp.allocate_tensors()
inp = interp.get_input_details()[0]
out = interp.get_output_details()[0]

passes = 0
for fname, expect in EXPECT.items():
    img = Image.open(f"{FIXDIR}/{fname}").convert("RGB").resize((224, 224))
    arr = (np.asarray(img, dtype="float32") / 255.0)[None, ...]
    interp.set_tensor(inp["index"], arr)
    interp.invoke()
    probs = interp.get_tensor(out["index"])[0]
    top = probs.argsort()[-3:][::-1]
    top1_ok = expect.lower() in labels[top[0]].lower()
    passes += top1_ok
    mark = "PASS" if top1_ok else "----"
    print(f"\n{mark}  {fname}  (expect ~'{expect}')")
    for rank, i in enumerate(top):
        print(f"    {rank+1}. {labels[i]:<42} {probs[i]*100:5.1f}%")

print(f"\n=== top-1 correct: {passes}/{len(EXPECT)} ===")
sys.exit(0)
