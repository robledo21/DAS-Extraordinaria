<?php
function sendFCMMessage($token, $title, $body) {
    $url = "https://fcm.googleapis.com/fcm/send";
    $serverKey = 'YOUR_SERVER_KEY';

    $notification = array('title' => $title, 'body' => $body);
    $arrayToSend = array('to' => $token, 'notification' => $notification, 'priority' => 'high');
    $json = json_encode($arrayToSend);

    $headers = array();
    $headers[] = 'Content-Type: application/json';
    $headers[] = 'Authorization: key=' . $serverKey;

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $json);

    $response = curl_exec($ch);
    curl_close($ch);
    return $response;
}

// Example usage
$token = "USER_DEVICE_TOKEN";
$title = "Test Notification";
$body = "This is a test notification from PHP";
$response = sendFCMMessage($token, $title, $body);
echo $response;
?>
