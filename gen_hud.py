import os
from PIL import Image, ImageDraw

def generate_hud():
    # 64x64 texture atlas for HUD elements
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Crosshair (Top-Left 16x16: 0,0 to 16,16)
    draw.line((8, 2, 8, 14), fill=(255, 255, 255, 200), width=2)
    draw.line((2, 8, 14, 8), fill=(255, 255, 255, 200), width=2)

    # Heart (Top-Right 16x16: 16,0 to 32,16)
    # Simple pixel heart
    heart_pixels = [
        (18, 3), (19, 2), (20, 2), (21, 3), (22, 4), (23, 4), (24, 4), (25, 4),
        (26, 3), (27, 2), (28, 2), (29, 3),
        (17, 4), (17, 5), (17, 6), (30, 4), (30, 5), (30, 6)
    ]
    draw.polygon([(23, 5), (18, 5), (18, 8), (23, 13), (29, 8), (29, 5)], fill=(200, 20, 20, 255))
    draw.polygon([(23, 5), (19, 3), (21, 3)], fill=(200, 20, 20, 255))
    draw.polygon([(24, 5), (28, 3), (26, 3)], fill=(200, 20, 20, 255))
    draw.rectangle([18, 4, 29, 8], fill=(200, 20, 20, 255))
    draw.polygon([(18, 8), (23, 13), (29, 8)], fill=(200, 20, 20, 255))

    # Hotbar Slot (Bottom-Left 32x32: 0,16 to 32,48)
    draw.rectangle((0, 16, 31, 47), fill=(100, 100, 100, 150), outline=(200, 200, 200, 255), width=2)
    
    # Selected Hotbar Slot (Bottom-Right 32x32: 32,16 to 64,48)
    draw.rectangle((32, 16, 63, 47), fill=(150, 150, 150, 150), outline=(255, 255, 50, 255), width=3)

    os.makedirs('src/main/resources/textures', exist_ok=True)
    img.save('src/main/resources/textures/hud.png')

if __name__ == '__main__':
    generate_hud()
