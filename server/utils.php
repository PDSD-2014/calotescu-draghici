<?php

$host = "localhost";
$user = "root";
$pass = "PDSD-2014-NeedEnergy";
$dbname = 'PDSD';
$charset = 'utf8';

// Save details about messages that have already been sent
function logMessage($table,$sender, $recv, $message, $reply_message) {
    GLOBAL $data;
    GLOBAL $db;

    if ($table == 0) {
        $query = "INSERT IGNORE INTO messages_log 
                        (id, sentdate, sender, receiver, message, google_reply) 
                  VALUES (NULL, NOW(), :sender, :recv, :message, :reply_message)";
    } else {
        $query = "INSERT IGNORE INTO ack_msg_log 
                        (id, sentdate, sender, receiver, message, google_reply) 
                  VALUES (NULL, NOW(), :sender, :recv, :message, :reply_message)";
    }

    try {
        $pdostmt = $db->prepare($query);
        $pdostmt->bindParam(':sender', $sender, PDO::PARAM_STR);
        $pdostmt->bindParam(':recv', $recv, PDO::PARAM_STR);
        $pdostmt->bindParam(':message', $message, PDO::PARAM_STR);        
        $pdostmt->bindParam(':reply_message', $reply_message, PDO::PARAM_STR);
        $pdostmt->execute();
    } catch (PDOException $e) {
        $data["status"] = "fail";  
        $data['message'][] = "Error while saving logs: ". $e->getMessage();
    }
}

function return_message() {//use $data for all return messages
    global $data;
    //http://stackoverflow.com/questions/13893361/access-control-allow-origin-localhost
    /* If a callback has been supplied then prepare to parse the callback
    ** function call back to browser along with JSON. */

    $jsonp = false;
    if (isset($_GET['callback'])) {
        $_GET['callback'] = strip_tags($_GET['callback']);
        $jsonp            = true;

        $pre = $_GET['callback'] . '(';
        $post = ');';
    }

    /* Encode JSON, and if jsonp is true, then ouput with the callback
    ** function; if not - just output JSON. */
    $json = json_encode($data, JSON_UNESCAPED_UNICODE);
    header('Content-Type:application/json');
    print(($jsonp) ? $pre . $json . $post : $json);
    
    //var_dump(json_decode(utf8_encode($data)));
    die;
}

?>
