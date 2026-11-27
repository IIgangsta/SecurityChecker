from PIL import Image, ImageDraw
import os

def create_icon(size, output_path):
    # Создаем прозрачное изображение
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Цвета (бирюзовый/зеленый фон как на вашем фото и белый значок)
    bg_color = (0, 168, 150, 255)  # Приятный бирюзовый
    fg_color = (255, 255, 255, 255) # Белый
    
    # Рисуем фон (квадрат, система сама скруглит углы благодаря adaptive icon)
    draw.rectangle([0, 0, size, size], fill=bg_color)
    
    # Координаты для щита (центр)
    cx, cy = size / 2, size / 2
    w = size * 0.5  # Ширина щита
    h = size * 0.6  # Высота щита
    thickness = int(size * 0.06) # Толщина линий

    # Рисуем контур щита
    # Верхняя часть (прямая)
    draw.line([(cx - w/2, cy - h/2), (cx + w/2, cy - h/2)], fill=fg_color, width=thickness)
    # Боковые стороны
    draw.line([(cx - w/2, cy - h/2), (cx - w/2, cy + h/4)], fill=fg_color, width=thickness)
    draw.line([(cx + w/2, cy - h/2), (cx + w/2, cy + h/4)], fill=fg_color, width=thickness)
    # Закругление внизу щита
    draw.arc([cx - w/2, cy + h/4 - w/2, cx + w/2, cy + h/4 + w/2], start=0, end=180, fill=fg_color, width=thickness)
    
    # Рисуем галочку внутри щита
    check_size = size * 0.25
    draw.line([(cx - check_size*0.4, cy), (cx - check_size*0.1, cy + check_size*0.4)], fill=fg_color, width=thickness)
    draw.line([(cx - check_size*0.1, cy + check_size*0.4), (cx + check_size*0.5, cy - check_size*0.3)], fill=fg_color, width=thickness)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

# Генерируем иконку для адаптивного фона (размер 432x432, стандарт для foreground)
create_icon(432, "app/src/main/res/drawable/ic_launcher_foreground.png")
print("Иконка успешно сгенерирована!")