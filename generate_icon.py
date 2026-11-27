import os
from PIL import Image, ImageDraw, ImageFont

def create_icon(size, output_path):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Светло-зеленый фон
    draw.ellipse((0, 0, size, size), fill=(164, 214, 164, 255))
    
    # Белая рамка (толстая)
    border_width = int(size * 0.08)
    draw.ellipse((border_width, border_width, size - border_width, size - border_width), outline=(255, 255, 255, 255), width=border_width)
    
    # Текст "SC"
    try:
        # Пытаемся найти шрифт, если нет — используем стандартный
        font = ImageFont.truetype("arial.ttf", int(size * 0.4))
    except IOError:
        font = ImageFont.load_default()
        
    text = "SC"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    x = (size - text_width) / 2
    y = (size - text_height) / 2 - (size * 0.05)
    draw.text((x, y), text, fill=(255, 255, 255, 255), font=font)
    
    img.save(output_path)

# Создаем папку, если ее нет
os.makedirs("app/src/main/res/mipmap-xxxhdpi", exist_ok=True)

# Генерируем иконку размером 512x512 (она будет использоваться как адаптивная)
create_icon(512, "app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png")
print("Иконка успешно сгенерирована!")