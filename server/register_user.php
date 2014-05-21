<?php

/*
 * Usage example:
 *     register_user.php?username=mihai_draghici&password=sda3423gfddd3123&android_id=blabglablaa
 */

require_once('utils.php');

// Connect to the database
try {
    $db = new PDO('mysql:host='.$host.';dbname='.$dbname.';charset='.$charset, $user, $pass);
    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $db->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
    $data["status"] = "ok";
} catch (PDOException $e) {
    $data['message'] = $e->getMessage();    
    $data['status'] = 'fail';
    return_message();
}

if (!isset($_REQUEST['username'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid username!';
    return_message();
}

if (!isset($_REQUEST['password'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid password!';
    return_message();
}

if (!isset($_REQUEST['android_id'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid android_id!';
    return_message();
}

$user = $_REQUEST['username'];
$pass = $_REQUEST['password'];

// Retrieve profile for a specified user (save in $data)
function get()
{
    GLOBAL $data;
    GLOBAL $db;
    GLOBAL $user;
    GLOBAL $currentPassword;

    $query = "SELECT id, password FROM user 
              WHERE username=:user";

    try {
        $pdostmt = $db->prepare($query);
        $pdostmt->bindParam(':user', $user, PDO::PARAM_STR);
        $pdostmt->execute();
      
        $row = $pdostmt->fetch(PDO::FETCH_ASSOC);
    } catch (PDOException $e) {
        $data['status'] = 'fail';  
        $data['message'] = $e->getMessage();
        return_message();
    }

    if ($row == FALSE) {
        $data['status'] = 'User not found';
    } else {
        $data['status'] = 'User found';  
        $data['userid'] = $row['id'];
        $currentPassword = $row['password'];
    }
}

$data["status"] = "ok";
// Check if the requested username is already registered
get();
if (strcmp($data['status'], 'User found') == 0) {
    $data['status'] = "fail";
    $data['message'] = "This username is already registered";
    unset($data['userid']);
    return_message();
}
$android_id = isset($_REQUEST['android_id']) ? $_REQUEST['android_id'] : '';

// Register the current user (email address) in the database
try {
    $pdostmt = $db->query('SELECT UUID()');
    $result = $pdostmt->fetch();    
    $generatedUUID = $result[0];

    $pdostmt = $db->prepare("INSERT INTO user(id, uid, android_id, username, password) 
                             VALUES (NULL, :uid, :android_id, :user, :password);");
    $pdostmt->bindparam(':uid', $generatedUUID, PDO::PARAM_STR);    
    $pdostmt->bindparam(':android_id', $android_id, PDO::PARAM_STR);
    $pdostmt->bindParam(':user', $user, PDO::PARAM_STR);
    $pdostmt->bindparam(':password', $pass, PDO::PARAM_STR);
    $pdostmt->execute();        
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'] = $e->getMessage();
    return_message();
}

// Return the registred user and return 'login_token'
$data['status'] = "ok";  
$data['uid'] = $generatedUUID;

require_once('track_request.php');
log_access('register_user.php', $_REQUEST);

return_message();
?>
