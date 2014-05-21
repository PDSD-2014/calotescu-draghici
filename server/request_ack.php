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
function send_message($registrationIDs) {
    global $data, $db, $mode;

	// Real BROWSER API key from Google APIs
	$apiKey = "AIzaSyCjq7HavxJHoM0CK4InR4tKqwAwJy9oHX0"; //mihai

	// Set POST variables
	$url = 'https://android.googleapis.com/gcm/send';

    $fields = array(
					'registration_ids'  => $registrationIDs,
					'data'              => array( "message" => "ACK_REQUEST", "sender" => "ACK_REQUEST"),
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
        logMessage(1,$sender_user, $recv_user, $message,  
                json_encode($result_decoded['results'][0], JSON_UNESCAPED_UNICODE));
    }
}

// Set current working directory
chdir(__DIR__);

// Get users ids
try {
    $pdostmt = $db->prepare("SELECT android_id FROM user");  
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'][] = "Error: ". $e->getMessage();
}
$result = $pdostmt->fetchAll(PDO::FETCH_ASSOC);
foreach ($result as $row) {
    $reg_ids[] = $row['android_id'];
}

// Send the ACK request
send_message($reg_ids);

$data['status'] = 'ok';
$data['registration_ids'] = $reg_ids;

print_r($data);
?>
