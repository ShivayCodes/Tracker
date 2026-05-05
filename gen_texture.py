import random
from PIL import Image

def generate_texture():
    # 5 tiles: Dirt, Grass, Stone, Wood, Leaves
    img = Image.new('RGB', (80, 16))
    pixels = img.load()

    # Dirt (0 to 15)
    for x in range(16):
        for y in range(16):
            base_r, base_g, base_b = 100, 70, 40
            noise = random.randint(-15, 15)
            pixels[x, y] = (base_r + noise, base_g + noise, base_b + noise)

    # Grass (16 to 31)
    for x in range(16):
        for y in range(16):
            if y < 4:
                base_r, base_g, base_b = 50, 150, 50
                noise = random.randint(-15, 15)
                pixels[x + 16, y] = (base_r + noise, base_g + noise, base_b + noise)
            elif y == 4:
                if random.random() > 0.5:
                    base_r, base_g, base_b = 50, 150, 50
                else:
                    base_r, base_g, base_b = 100, 70, 40
                noise = random.randint(-15, 15)
                pixels[x + 16, y] = (base_r + noise, base_g + noise, base_b + noise)
            else:
                base_r, base_g, base_b = 100, 70, 40
                noise = random.randint(-15, 15)
                pixels[x + 16, y] = (base_r + noise, base_g + noise, base_b + noise)

    # Stone (32 to 47)
    for x in range(16):
        for y in range(16):
            base_gray = 120
            noise = random.randint(-20, 20)
            if random.random() > 0.95:
                noise -= 40
            val = base_gray + noise
            pixels[x + 32, y] = (val, val, val)

    # Wood (48 to 63)
    for x in range(16):
        for y in range(16):
            base_r, base_g, base_b = 80, 50, 20
            noise = random.randint(-10, 10)
            if x % 4 == 0: # Grain
                noise -= 15
            pixels[x + 48, y] = (base_r + noise, base_g + noise, base_b + noise)

    # Leaves (64 to 79)
    for x in range(16):
        for y in range(16):
            base_r, base_g, base_b = 30, 100, 30
            noise = random.randint(-20, 20)
            if random.random() > 0.8:
                noise += 15
            pixels[x + 64, y] = (base_r + noise, base_g + noise, base_b + noise)

    img.save('src/main/resources/textures/atlas.png')

if __name__ == '__main__':
    generate_texture()
