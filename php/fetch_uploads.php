<?php
// Directorio donde se almacenan las fotos
$uploadDirectory = 'uploads/';

// Array para almacenar las URL de las fotos
$photoUrls = array();

// Escanear el directorio de subidas
$files = scandir($uploadDirectory);

// Iterar sobre los archivos encontrados y agregar sus URL al array
foreach ($files as $file) {
    if ($file !== '.' && $file !== '..') {
        $photoUrls[] = $uploadDirectory . $file;
    }
}

// Devolver el array de URLs como JSON
echo json_encode($photoUrls);
?>
