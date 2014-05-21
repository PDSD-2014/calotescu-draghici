#!/bin/bash
#This script run every 2 seconds
while (sleep 2 && php /var/www/html/PDSD/request_ack.php && php /var/www/html/PDSD/update_db.php) &
do
  wait $!
done
