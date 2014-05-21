<?php

require_once('utils.php');

$current_time = date('Y-m-d H:i:s', time());   

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

// Set status = 0 for all users
$status = 0;
$query = "UPDATE user SET status=:status";
try {
    $pdostmt = $db->prepare($query);
    $pdostmt->bindParam(':status', $status, PDO::PARAM_INT);
    $pdostmt->execute();
} catch (PDOException $e) {
    $data["status"] = "fail";  
    $data['message'] = $e->getMessage();
    return $data;
}

// Get all users
$query = "SELECT id, access_time from user";
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

// SET status = 1 for active users;
$data['status'] = 'ok';
$current_time = date('Y-m-d H:i:s', time()); 
foreach ($result as $row) {
    //echo strtotime($current_time) - strtotime($row['access_time']);    
    if (strtotime($current_time) - strtotime($row['access_time']) < 10) {
        $query = "UPDATE user SET status=:status
                  WHERE id = :id";
        $status = 1;
        try {
            $pdostmt = $db->prepare($query);
            $pdostmt->bindParam(':status', $status, PDO::PARAM_INT);
            $pdostmt->bindParam(':id', $row['id'], PDO::PARAM_INT);    
            $pdostmt->execute();
        } catch (PDOException $e) {
            $data["status"] = "fail";  
            $data['message'] = $e->getMessage();
            return_message();
        }
    }
}

print_r($data);
?>
