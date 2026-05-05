import random
from PIL import Image

def generate_texture():
    img = Image.new('RGB', (48, 16))
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
                # Top grass
                base_r, base_g, base_b = 50, 150, 50
                noise = random.randint(-15, 15)
                pixels[x + 16, y] = (base_r + noise, base_g + noise, base_b + noise)
            elif y == 4:
                # Grass edge
                if random.random() > 0.5:
                    base_r, base_g, base_b = 50, 150, 50
                else:
                    base_r, base_g, base_b = 100, 70, 40
                noise = random.randint(-15, 15)
                pixels[x + 16, y] = (base_r + noise, base_g + noise, base_b + noise)
            else:
                # Dirt bottom
                base_r, base_g, base_b = 100, 70, 40
                noise = random.randint(-15, 15)
                pixels[x + 16, y] = (base_r + noise, base_g + noise, base_b + noise)

    # Stone (32 to 47)
    for x in range(16):
        for y in range(16):
            base_gray = 120
            noise = random.randint(-20, 20)
            
            # Add some "cracks" randomly
            if random.random() > 0.95:
                noise -= 40
            
            val = base_gray + noise
            pixels[x + 32, y] = (val, val, val)

    img.save('src/main/resources/textures/atlas.png')

if __name__ == '__main__':
    generate_texture()
