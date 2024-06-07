<?php
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['image'])) {
        $image = $_POST['image'];
        $decodedImage = base64_decode($image);
        $filename = uniqid() . '.jpg';
        $filepath = 'uploads/' . $filename;

        if (!file_exists('uploads')) {
            mkdir('uploads', 0777, true);
        }

        if (file_put_contents($filepath, $decodedImage)) {
            echo json_encode(['status' => 'success', 'message' => 'Image uploaded successfully', 'filename' => $filename]);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Failed to upload image']);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Image data not provided']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method']);
}
?>
