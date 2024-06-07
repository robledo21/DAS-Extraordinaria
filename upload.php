<?php
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Leer el cuerpo de la solicitud para obtener los datos de la imagen
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);
    
    if (isset($data['image'])) {
        $image = $data['image'];
        $decodedImage = base64_decode($image);
        
        // Asegurarse de que la decodificación se realizó correctamente
        if ($decodedImage === false) {
            echo json_encode(['status' => 'error', 'message' => 'Base64 decoding failed']);
            exit;
        }

        // Definir el nombre y la ruta del archivo de imagen
        $filename = uniqid() . '.png';
        $filepath = 'uploads/' . $filename;

        // Crear el directorio de carga si no existe
        if (!file_exists('uploads')) {
            mkdir('uploads', 0777, true);
        }

        // Guardar la imagen en el servidor
        $result = file_put_contents($filepath, $decodedImage);

        // Verificar que la imagen se guardó correctamente
        if ($result !== false) {
            echo json_encode(['status' => 'success', 'message' => 'Image uploaded successfully', 'filename' => $filename]);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Failed to save the image']);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Image data not provided']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method']);
}
?>

