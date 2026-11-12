#!/usr/bin/env python3
"""Regenerate platform icons from the copied Apple artwork. Requires Pillow and macOS iconutil."""
from pathlib import Path
import argparse
import subprocess

from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ICONS = ROOT / "assets/icons"
ICONSET = ROOT / ".tooling/FlowPlay.iconset"
RES = ROOT / "androidApp/src/main/res"
TRANSPARENT = ICONS / "flowplay-icon-transparent.png"
OPAQUE = ICONS / "flowplay-icon-original.jpg"


def reference_background(reference):
    """Match the Apple icon generator's smooth four-edge background interpolation."""
    reference = reference.filter(ImageFilter.GaussianBlur(24))
    last = reference.width - 1
    top_left = reference.getpixel((0, 0))
    top_right = reference.getpixel((last, 0))
    bottom_left = reference.getpixel((0, last))
    bottom_right = reference.getpixel((last, last))
    patch = Image.new("RGB", (128, 128))
    pixels = []
    for row in range(128):
        vertical = row / 127
        left = reference.getpixel((0, round(vertical * last)))
        right = reference.getpixel((last, round(vertical * last)))
        for column in range(128):
            horizontal = column / 127
            top = reference.getpixel((round(horizontal * last), 0))
            bottom = reference.getpixel((round(horizontal * last), last))
            color = []
            for channel in range(3):
                corners = (
                    (1 - horizontal) * (1 - vertical) * top_left[channel]
                    + horizontal * (1 - vertical) * top_right[channel]
                    + (1 - horizontal) * vertical * bottom_left[channel]
                    + horizontal * vertical * bottom_right[channel]
                )
                value = (
                    (1 - vertical) * top[channel] + vertical * bottom[channel]
                    + (1 - horizontal) * left[channel] + horizontal * right[channel] - corners
                )
                color.append(max(0, min(255, round(value))))
            pixels.append(tuple(color))
    patch.putdata(pixels)
    return patch.resize((1024, 1024), Image.Resampling.BICUBIC)


def generate_tv_banner(mark, background):
    banner = background.resize((1280, 720), Image.Resampling.LANCZOS).convert("RGBA")
    artwork = ImageOps.contain(mark, (400, 440), Image.Resampling.LANCZOS)
    banner.alpha_composite(artwork, (88, (720 - artwork.height) // 2))
    draw = ImageDraw.Draw(banner)
    font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Bold.ttf", 120)
    draw.text((564, 360), "FlowPlay", font=font, fill="white", anchor="lm",
              stroke_width=1, stroke_fill="white")
    directory = RES / "drawable-xhdpi"
    directory.mkdir(parents=True, exist_ok=True)
    banner.convert("RGB").resize((320, 180), Image.Resampling.LANCZOS).save(directory / "tv_banner.png")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tv-banner-only", action="store_true")
    args = parser.parse_args()
    source = Image.open(TRANSPARENT).convert("RGBA")
    mark = source.crop(source.getchannel("A").getbbox())
    artwork = ImageOps.contain(mark, (819, 819), Image.Resampling.LANCZOS)
    transparent = Image.new("RGBA", (1024, 1024))
    transparent.alpha_composite(artwork, ((1024 - artwork.width) // 2, (1024 - artwork.height) // 2))
    original = Image.open(OPAQUE).convert("RGB")
    background = reference_background(original)
    generate_tv_banner(mark, background)
    if args.tv_banner_only:
        return
    opaque = Image.alpha_composite(background.convert("RGBA"), transparent).convert("RGB")
    ICONSET.mkdir(parents=True, exist_ok=True)
    for size in (16, 32, 128, 256, 512):
        for scale in (1, 2):
            suffix = "@2x" if scale == 2 else ""
            transparent.resize((size * scale, size * scale), Image.Resampling.LANCZOS).save(
                ICONSET / f"icon_{size}x{size}{suffix}.png"
            )
    subprocess.run(["iconutil", "-c", "icns", str(ICONSET), "-o", str(ICONS / "FlowPlay.icns")], check=True)
    transparent.save(ICONS / "FlowPlay.ico", sizes=[(n, n) for n in (16, 24, 32, 48, 64, 128, 256)])
    shared = ROOT / "shared/src/commonMain/composeResources/drawable"
    shared.mkdir(parents=True, exist_ok=True)
    transparent.save(shared / "flowplay_icon.png")
    for density, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
        directory = RES / f"mipmap-{density}"
        directory.mkdir(parents=True, exist_ok=True)
        opaque.resize((size, size), Image.Resampling.LANCZOS).save(directory / "ic_launcher.png")
    # Keep the transparent mark inside Android's 66/108 adaptive-icon safe region.
    foreground = Image.new("RGBA", (432, 432))
    adaptive_mark = ImageOps.contain(mark, (264, 264), Image.Resampling.LANCZOS)
    foreground.alpha_composite(adaptive_mark, ((432 - adaptive_mark.width) // 2, (432 - adaptive_mark.height) // 2))
    drawable = RES / "drawable-nodpi"
    drawable.mkdir(parents=True, exist_ok=True)
    foreground.save(drawable / "ic_launcher_foreground.png")
    background.resize((432, 432), Image.Resampling.LANCZOS).save(drawable / "ic_launcher_background_image.png")


if __name__ == "__main__":
    main()
