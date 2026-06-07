#!/usr/bin/env python3
"""Grid-tiling TTA option diagram (PLANTPOTTING-0012 Phase 7)."""
import os
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(__file__)
OUT = os.path.join(HERE, "tta-diagrams")
FIX = os.path.abspath(os.path.join(HERE, "..", "..", "..", "..",
      "app", "src", "androidTest", "assets", "identify-fixtures", "pilea-peperomioides__03.jpg"))
S = 480
def font(sz):
    for p in (r"C:\Windows\Fonts\arialbd.ttf", r"C:\Windows\Fonts\arial.ttf"):
        if os.path.exists(p):
            return ImageFont.truetype(p, sz)
    return ImageFont.load_default()
def base_frame(dim=0.4):
    img = Image.open(FIX).convert("RGB").resize((S, S))
    return Image.blend(img, Image.new("RGB", (S, S), (20, 20, 20)), dim)

def grid_panel(n, color, tilepx):
    im = base_frame()
    d = ImageDraw.Draw(im, "RGBA")
    step = S / n
    k = 1
    for r in range(n):
        for c in range(n):
            x0, y0 = int(c * step), int(r * step)
            x1, y1 = int((c + 1) * step), int((r + 1) * step)
            d.rectangle([x0, y0, x1 - 1, y1 - 1], outline=color, width=3)
            lab = str(k)
            f = font(20)
            tw = d.textlength(lab, font=f)
            cx, cy = (x0 + x1) / 2 - tw / 2, (y0 + y1) / 2 - 12
            d.rectangle([cx - 3, cy - 2, cx + tw + 3, cy + 22], fill=(0, 0, 0))
            d.text((cx, cy), lab, fill=color, font=f)
            k += 1
    return im

panels = [
    (grid_panel(2, (90, 200, 255), 240), "2x2 = 4 tiles, 240px each\n(~1.0x zoom — whole-plant gestalt kept)"),
    (grid_panel(3, (120, 255, 120), 160), "3x3 = 9 tiles, 160px each\n(~1.4x zoom — clusters of leaves)"),
    (grid_panel(4, (255, 120, 255), 120), "4x4 = 16 tiles, 120px each\n(~1.9x zoom — single leaf, little context)"),
]
pad, hdr_h, cap_h = 16, 76, 58
w = pad + sum(p[0].width + pad for p in panels)
h = hdr_h + S + cap_h + pad
canvas = Image.new("RGB", (w, h), (250, 250, 250))
d = ImageDraw.Draw(canvas)
d.text((pad, 8), "OPTION: UNIFORM GRID TILING (base 6 + every grid cell)", fill=(15, 15, 15), font=font(28))
d.text((pad, 42), "Non-overlapping, full coverage, zero re-score. Tradeoff: finer grid = smaller tiles = the model "
       "loses whole-plant context (each tile upscaled to the 224px input).", fill=(90, 90, 90), font=font(17))
x = pad
for img, cap in panels:
    canvas.paste(img, (x, hdr_h))
    for i, line in enumerate(cap.split("\n")):
        d.text((x, hdr_h + S + 6 + i * 24), line, fill=(15, 15, 15), font=font(18))
    x += img.width + pad
canvas.save(os.path.join(OUT, "tta_D_grid.png"))
print("wrote tta_D_grid.png", canvas.size)
