import os
import re

def remove_comments(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        lines = file.readlines()
    
    modified_lines = []
    in_string = False
    in_multiline_comment = False
    modified = False
    
    for line in lines:
        new_line = ""
        i = 0
        while i < len(line):
            # Проверка на начало многострочного комментария
            if not in_string and line[i:i+2] == "/*":
                in_multiline_comment = True
                i += 2
                modified = True
                continue
            
            # Проверка на конец многострочного комментария
            if in_multiline_comment and line[i:i+2] == "*/":
                in_multiline_comment = False
                i += 2
                modified = True
                continue
            
            # Пропускаем символы в многострочном комментарии
            if in_multiline_comment:
                i += 1
                modified = True
                continue
            
            # Проверка на начало однострочного комментария
            if not in_string and line[i:i+2] == "//":
                modified = True
                break  # Игнорируем остаток строки
            
            # Проверка на начало/конец строки
            if line[i] == '"' and (i == 0 or line[i-1] != '\\'):
                in_string = not in_string
            
            new_line += line[i]
            i += 1
        
        modified_lines.append(new_line)
    
    # Если были изменения, записываем обратно в файл
    if modified:
        with open(file_path, 'w', encoding='utf-8') as file:
            file.writelines(modified_lines)
        print(f"Комментарии удалены: {file_path}")
    
    return modified

def process_directory(directory):
    files_processed = 0
    files_modified = 0
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                files_processed += 1
                if remove_comments(file_path):
                    files_modified += 1
    
    return files_processed, files_modified

if __name__ == "__main__":
    java_dir = "src/main/java/me/wth/ultimaCore"
    print(f"Начинаем удаление комментариев в директории: {java_dir}")
    processed, modified = process_directory(java_dir)
    print(f"Готово! Обработано файлов: {processed}, изменено файлов: {modified}") 