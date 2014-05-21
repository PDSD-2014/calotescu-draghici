<?php

/*
 * Usage example:
 *      login_user.php?username=mihai_draghici&password=sda3423gfddd3123
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
    $data["status"] = "fail";
    return_message();
}

if (!isset($_REQUEST['username'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid username!';
    return_message();
}

if (!isset($_REQUEST['android_id'])) {
    $data['status'] = 'fail';
    $data['message'] = 'Invalid android_id!';
    return_message();
}

$user = $_REQUEST['username'];
$pass = isset($_REQUEST['password']) ? $_REQUEST['password'] : '';
$android_id = $_REQUEST['android_id'];

// Password login
$query = "select uid, password FROM user where username = :user";
try {
    $pdostmt = $db->prepare($query);
    $pdostmt->bindParam(':user', $_REQUEST['username'], PDO::PARAM_STR);
    $pdostmt->execute();

    $row = $pdostmt->fetch(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    $data["status"] = "error";  
    $data['message'] = $e->getMessage();
    return_message();
}

// if the requested user does not exist in the database - Fail
if ($row == FALSE) {
    $data['status'] = "fail";
    $data['message'] = "Incorrect username!";
    return_message();
}

// If the password is incorrect - fail (password login)
if (strcmp($row['password'], $pass) != 0) {
    $data['status'] = "fail";
    $data['message'] = "Incorrect password!";
    return_message();
}

// If everything is ok return login_token and update android_id
$query = "UPDATE user
          SET android_id = :android_id
          WHERE uid = :uid";
try {
    $pdostmt = $db->prepare($query);
    $pdostmt->bindParam(':android_id', $android_id, PDO::PARAM_STR);
    $pdostmt->bindParam(':uid', $row['uid'], PDO::PARAM_STR);    
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "error";  
    $data['message'] = $e->getMessage();
    return_message();
}
$data['status'] = "ok";
$data['uid'] = $row['uid'];

require_once('track_request.php');
log_access('login_user.php', $_REQUEST);

return_message();
?>
