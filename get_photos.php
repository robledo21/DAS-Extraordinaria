<?php
header('Content-Type: application/json');

$directory = 'uploads/';
$images = [];

// Abrir el directorio
if (is_dir($directory)) {
    if ($dh = opendir($directory)) {
        // Leer los archivos del directorio
        while (($file = readdir($dh)) !== false) {
            // Solo agregar archivos de imagen (puedes ajustar esta condición según tus necesidades)
            if (in_array(pathinfo($file, PATHINFO_EXTENSION), ['jpg', 'jpeg', 'png', 'gif'])) {
                $images[] = 'http://' . $_SERVER['HTTP_HOST'] . '/' . $directory . $file;
            }
        }
        closedir($dh);
    }
}

// Devolver los URLs de las imágenes en formato JSON
echo json_encode($images);
?>
