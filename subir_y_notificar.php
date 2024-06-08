<?php
require __DIR__ . '/vendor/autoload.php';

use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

// Inicializa Firebase con el archivo de credenciales
$factory = (new Factory)->withServiceAccount('/var/www/html/firebase_credentials.json');
$messaging = $factory->createMessaging();

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Leer el cuerpo de la solicitud para obtener los datos de la imagen y el token
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);

    if (isset($data['image']) && isset($data['token'])) {
        $image = $data['image'];
        $token = $data['token'];
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
            echo json_encode(["status" => "success", "message" => "Image uploaded"]);

            // Crear el mensaje de notificación
            $message = CloudMessage::fromArray([
                'token' => $token, // Usar el token del dispositivo
                'notification' => [
                    'title' => 'Imagen subida!',
                    'body' => 'Se ha subido correctamente la foto',
                ],
                'data' => [
                    'imageUrl' => 'http://' . $_SERVER['HTTP_HOST'] . '/' . $filepath
                ],
            ]);

            try {
                // Enviar la notificación
                $messaging->send($message);
                echo json_encode(["status" => "success", "message" => "Image uploaded and notification sent + token: $token", "filename" => $filename]);
            } catch (Exception $e) {
                echo json_encode(["status" => "success", "message" => "Image uploaded but Failed to send notification", "error" => $e->getMessage()]);
            }
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Failed to save the image']);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Image data or token not provided']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method']);
}
?>
