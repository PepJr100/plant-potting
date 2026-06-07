#!/usr/bin/env python3
"""Render labelled TTA crop-zone diagrams for the PLANTPOTTING-0012 Phase 7 strategy choice."""
import os
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(__file__)
OUT = os.path.join(HERE, "tta-diagrams")
os.makedirs(OUT, exist_ok=True)
FIX = os.path.abspath(os.path.join(HERE, "..", "..", "..", "..",
      "app", "src", "androidTest", "assets", "identify-fixtures", "pilea-peperomioides__03.jpg"))

S = 480
def font(sz):
    for p in (r"C:\Windows\Fonts\arialbd.ttf", r"C:\Windows\Fonts\arial.ttf"):
        if os.path.exists(p):
            return ImageFont.truetype(p, sz)
    return ImageFont.load_default()

def base_frame(dim=0.45):
    img = Image.open(FIX).convert("RGB").resize((S, S))
    # dim so overlays read clearly
    return Image.blend(img, Image.new("RGB", (S, S), (20, 20, 20)), dim)

def rect(draw, box, color, label, lw=4, anchor="tl"):
    x0, y0, x1, y1 = box
    draw.rectangle(box, outline=color, width=lw)
    f = font(22)
    tw = draw.textlength(label, font=f)
    pad = 4
    if anchor == "tl":
        lx, ly = x0 + 3, y0 + 3
    elif anchor == "ctr":
        lx, ly = (x0 + x1) / 2 - tw / 2, (y0 + y1) / 2 - 13
    elif anchor == "br":
        lx, ly = x1 - tw - 6, y1 - 28
    else:
        lx, ly = x0 + 3, y0 + 3
    draw.rectangle([lx - pad, ly - pad, lx + tw + pad, ly + 26], fill=(0, 0, 0))
    draw.text((lx, ly), label, fill=color, font=f)

def titled(panels, title, subtitle, fname):
    """panels: list of (PIL image, caption). Compose horizontally with a header."""
    pad, cap_h, hdr_h = 16, 34, 70
    w = pad + sum(p[0].width + pad for p in panels)
    h = hdr_h + S + cap_h + pad
    canvas = Image.new("RGB", (w, h), (250, 250, 250))
    d = ImageDraw.Draw(canvas)
    d.text((pad, 10), title, fill=(15, 15, 15), font=font(30))
    d.text((pad, 44), subtitle, fill=(90, 90, 90), font=font(18))
    x = pad
    for img, cap in panels:
        canvas.paste(img, (x, hdr_h))
        cf = font(19)
        d.text((x, hdr_h + S + 6), cap, fill=(15, 15, 15), font=cf)
        x += img.width + pad
    canvas.save(os.path.join(OUT, fname))
    print("wrote", fname, canvas.size)

# colours
C_FULL, C_CTR, C_CORNER = (255, 90, 90), (90, 200, 255), (120, 255, 120)
C_EDGE, C_GRID = (255, 200, 0), (255, 120, 255)
C_TIGHT, C_LOOSE = (255, 120, 255), (120, 255, 120)

corner = int(0.8 * S)
mx, my = S - corner, S - corner

# ---------- BASE 6 (reference) ----------
img = base_frame()
d = ImageDraw.Draw(img, "RGBA")
rect(d, (0, 0, S - 1, S - 1), C_FULL, "1 FULL", lw=6, anchor="br")
rect(d, (0, 0, corner, corner), C_CORNER, "3 TL")
rect(d, (mx, 0, S, corner), C_CORNER, "4 TR")
rect(d, (0, my, corner, S), C_CORNER, "5 BL")
rect(d, (mx, my, S, S), C_CORNER, "6 BR")
c0 = (S - corner) // 2
rect(d, (c0, c0, c0 + corner, c0 + corner), C_CTR, "2 CENTRE", anchor="ctr")
titled([(img, "full + centre + 4 corners (0.8x)")],
       "BASE 6  (shipped tta=6 — unchanged in every option)",
       "These 6 views always run. The strategies below only define views 7+.",
       "tta_base6.png")

# ---------- A: NEW SPATIAL ZONES ----------
# panel A1 — edge midpoints
a1 = base_frame()
d = ImageDraw.Draw(a1, "RGBA")
half = (S - corner) // 2
rect(d, (half, 0, half + corner, corner), C_EDGE, "7 TOP-C")
rect(d, (half, my, half + corner, S), C_EDGE, "8 BOT-C")
rect(d, (0, half, corner, half + corner), C_EDGE, "9 L-MID")
rect(d, (mx, half, S, half + corner), C_EDGE, "10 R-MID")
# panel A2 — offset 0.5x grid (sample the gaps)
a2 = base_frame()
d = ImageDraw.Draw(a2, "RGBA")
g = int(0.5 * S)
positions = [(S//2 - g//2, 0), (0, S//2 - g//2), (S - g, S//2 - g//2),
             (S//2 - g//2, S - g), (S//2 - g//2, S//2 - g//2)]
labs = ["11", "12", "13", "14", "15(+)"]
for (px, py), lb in zip(positions, labs):
    rect(d, (px, py, px + g, py + g), C_GRID, lb, lw=3)
titled([(a1, "views 7-10: EDGE MIDPOINTS (0.8x) — new zones"),
        (a2, "views 11-20: OFFSET 0.5x GRID — samples the gaps")],
       "STRATEGY A — NEW SPATIAL ZONES",
       "Every added view covers area the centre/corner set MISSES. No zone is re-scored.",
       "tta_A_new_zones.png")

# ---------- B: MULTI-SCALE PYRAMID ----------
def scaled_set(frac, color, tag):
    im = base_frame()
    d = ImageDraw.Draw(im, "RGBA")
    sq = int(frac * S)
    off = (S - sq) // 2
    # centre + 4 corners at this scale
    rect(d, (off, off, off + sq, off + sq), color, f"{tag} CTR", anchor="ctr")
    rect(d, (0, 0, sq, sq), color, f"{tag} TL", lw=3)
    rect(d, (S - sq, 0, S, sq), color, f"{tag} TR", lw=3)
    rect(d, (0, S - sq, sq, S), color, f"{tag} BL", lw=3)
    rect(d, (S - sq, S - sq, S, S), color, f"{tag} BR", lw=3)
    return im
b1 = scaled_set(0.65, C_TIGHT, "7-11")
b2 = scaled_set(0.90, C_LOOSE, "12-16")
titled([(b1, "views 7-11: centre+corners at 0.65x (tight/detail)"),
        (b2, "views 12-16: centre+corners at 0.90x (loose/context)")],
       "STRATEGY B — MULTI-SCALE PYRAMID",
       "Same crop CENTRES as the base set, but new zoom levels (detail<->context). Same zones, new scale.",
       "tta_B_multiscale.png")

# ---------- C: HORIZONTAL FLIPS ----------
c1 = base_frame()
d = ImageDraw.Draw(c1, "RGBA")
rect(d, (0, 0, corner, corner), C_CORNER, "TL")
rect(d, (mx, 0, S, corner), C_CORNER, "TR")
c0 = (S - corner) // 2
rect(d, (c0, c0, c0 + corner, c0 + corner), C_CTR, "CTR", anchor="ctr")
c2 = c1.transpose(Image.FLIP_LEFT_RIGHT)
d2 = ImageDraw.Draw(c2, "RGBA")
d2.text((10, 10), "<- MIRRORED ->", fill=(255, 255, 0), font=font(26))
titled([(c1, "base zones (views 1-6)"),
        (c2, "views 7-12: same zones, left-right MIRRORED")],
       "STRATEGY C — HORIZONTAL FLIPS",
       "Mirror the base 6 (and extras). Cheapest + empirically strongest TTA, but it is the SAME zones flipped.",
       "tta_C_flips.png")
print("done ->", OUT)
