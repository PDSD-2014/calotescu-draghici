<?php

require_once('utils.php');

// Connect to DB
try {
    $db = new PDO('mysql:host='.$host.';dbname='.$dbname.';charset='.$charset, $user, $pass);
    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
    $data["status"] = "ok";
} catch (PDOException $e) {
    $data['message'] = $e->getMessage();    
    $data["status"] = "fail";
    return_message();
}

// Android Push notification 
function send_message($recv_android_id, $sender_user, $recv_user, $message) {
    global $data, $db, $mode;

	// Real BROWSER API key from Google APIs
	$apiKey = "AIzaSyCjq7HavxJHoM0CK4InR4tKqwAwJy9oHX0"; //mihai

	// Set POST variables
	$url = 'https://android.googleapis.com/gcm/send';

    $registrationIDs[] = $recv_android_id;
	$fields = array(
					'registration_ids'  => $registrationIDs,
					'data'              => array( "message" => $message, "sender" => $sender_user),
					);

	$headers = array( 
					'Authorization: key=' . $apiKey,
					'Content-Type: application/json'
					);

	// Open connection
	$ch = curl_init();

	// Set the url, number of POST vars, POST data
	curl_setopt( $ch, CURLOPT_URL, $url );

	curl_setopt( $ch, CURLOPT_POST, true );
	curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );

	curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );

	// Execute post
	$result = curl_exec($ch);

	// Close connection
	curl_close($ch);
    
	// check results for error codes
    if ($result == FALSE) {
        $data['message'] = "Failed to send the message";           
        $data["status"] = "fail";
    } else {
        $result_decoded = json_decode($result, true);
        logMessage(0,$sender_user, $recv_user, $message,  
                json_encode($result_decoded['results'][0], JSON_UNESCAPED_UNICODE));
    }
}

// Set current working directory
chdir(__DIR__);
if (!isset($_REQUEST['sender_id'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid sender_id!';
    return_message();
}

if (!isset($_REQUEST['receiver'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid receiver username!';
    return_message();
}

if (!isset($_REQUEST['message'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Empty message!';
    return_message();
}

$sender_uid = $_REQUEST['sender_id'];
$recv_user = $_REQUEST['receiver'];
$message = $_REQUEST['message'];

// Get receiver android_id
try {
    $pdostmt = $db->prepare("SELECT android_id FROM user u 
                             WHERE u.username = :user");        
    $pdostmt->bindParam(':user', $recv_user, PDO::PARAM_STR);
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'][] = "Error: ". $e->getMessage();
}
$row = $pdostmt->fetch(PDO::FETCH_ASSOC);
$recv_android_id = $row['android_id'];

// Get sender username
try {
    $pdostmt = $db->prepare("SELECT username FROM user u 
                             WHERE u.uid = :uid");        
    $pdostmt->bindParam(':uid', $sender_uid, PDO::PARAM_STR);
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'][] = "Error: ". $e->getMessage();
}
$row = $pdostmt->fetch(PDO::FETCH_ASSOC);
$sender_user = $row['username'];

// Check sender
if ($sender_user == '') {
    $data['status'] = 'fail';
    $data['message'] = 'Sender was not found!';
    return_message();
}

// Check receiver
if ($recv_android_id == '') {
    $data['status'] = 'fail';
    $data['message'] = 'Receiver was not found!';
    return_message();
}

// Send the message
send_message($recv_android_id, $sender_user, $recv_user, $message);

$data['status'] = 'ok';

require_once('track_request.php');
log_access('send_message.php', $_REQUEST);

return_message();
?>
