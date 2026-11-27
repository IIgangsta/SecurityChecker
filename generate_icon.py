from PIL import Image, ImageDraw
import os

def create_icon(size, output_path):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Цвета
    bg_color = (0, 168, 150, 255)  # Бирюзовый фон
    fg_color = (255, 255, 255, 255) # Белый значок
    
    # 1. Рисуем фон
    draw.rectangle([0, 0, size, size], fill=bg_color)
    
    # 2. Рисуем щит (используем полигон для надежности)
    # Масштабируем координаты относительно размера картинки
    w = size * 0.45
    h = size * 0.55
    cx, cy = size / 2, size / 2
    
    # Точки для контура щита (верх, бока, заострение внизу)
    shield_points = [
        (cx - w/2, cy - h/2), # Верхний левый
        (cx + w/2, cy - h/2), # Верхний правый
        (cx + w/2, cy + h/4), # Правый бок
        (cx, cy + h/2),       # Острие внизу
        (cx - w/2, cy + h/4)  # Левый бок
    ]
    
    # Рисуем заливку щита (толстый контур)
    draw.polygon(shield_points, outline=fg_color, width=int(size * 0.08))
    
    # 3. Рисуем галочку внутри щита
    check_w = size * 0.15
    check_h = size * 0.15
    
    # Точки галочки
    check_points = [
        (cx - check_w/2, cy),             # Левая точка
        (cx - check_w/4, cy + check_h/2), # Нижняя точка
        (cx + check_w/2, cy - check_h/2)  # Правая верхняя точка
    ]
    
    draw.line(check_points, fill=fg_color, width=int(size * 0.08), joint="curve")

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

# Генерируем иконку
create_icon(432, "app/src/main/res/drawable/ic_launcher_foreground.png")
print("Иконка успешно сгенерирована!")