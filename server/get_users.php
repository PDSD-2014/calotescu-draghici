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

if (!isset($_REQUEST['username'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid username!';
    return_message();
}
$username = $_REQUEST['username'];

// Get available users
$query = "SELECT username, status from user
          WHERE NOT username = :username";
try {
    $pdostmt = $db->prepare($query);
    $pdostmt->bindParam(':username', $username, PDO::PARAM_STR);    
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'] = $e->getMessage();
    return_message();
}
$result = $pdostmt->fetchAll(PDO::FETCH_ASSOC);

$data['status'] = 'ok';
foreach ($result as $row) {
    $current_user['username'] = $row['username'];
    $current_user['status'] = $row['status']; 
    $data['users'][] = $current_user;
}

$current_time = date('Y-m-d H:i:s', time()); 
$query = "UPDATE user SET access_time=:time
          WHERE username = :username";
try {
    $pdostmt = $db->prepare($query);
    $pdostmt->bindParam(':time', $current_time, PDO::PARAM_STR); 
    $pdostmt->bindParam(':username', $username, PDO::PARAM_STR);    
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'] = $e->getMessage();
    return_message();
}

require_once('track_request.php');
log_access('get_users.php', $_REQUEST);

return_message();
?>
