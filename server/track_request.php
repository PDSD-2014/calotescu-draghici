<?php

function log_access($script, $arg) {  
    global $db;

    date_default_timezone_set("America/New_York");
    $current_time = date('Y-m-d H:i:s', time());   

    // Get arguments used for the initial script
    $text = "";
    foreach ($arg as $key => $value) {
        if ($key !=  "uid") {
            $val = $value;
            if (is_array($value)) {
                $val = implode(",", $value);
            }
            $text .= $key . "=" . $val . " ";
        }    
    }

    $uid = isset($arg['uid']) ? $arg['uid'] : "";

    // Log all acceses that come from the device via the api
    $query = "INSERT INTO requests_logs (id, time, script, uid, arguments)
              VALUES (NULL, :time, :script, :uid, :arguments)";
    try {
        $pdostmt = $db->prepare($query);
        $pdostmt->bindParam(':time', $current_time, PDO::PARAM_STR);
        $pdostmt->bindParam(':script', $script, PDO::PARAM_STR);
        $pdostmt->bindParam(':uid', $uid, PDO::PARAM_STR);
        $pdostmt->bindParam(':arguments', $text, PDO::PARAM_STR);
        $pdostmt->execute();
    } catch (PDOException $e) {
        $data["status"] = "fail";  
        $data['message'] = $e->getMessage();
        return $data;
    }

    $data["status"] = "ok";
    $db = null;

    return $data;
}

?>
