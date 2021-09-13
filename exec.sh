#!/bin/bash

semaphore_ip="127.0.0.1"
semaphore_port="52369"

server_ip="127.0.0.1"
server_port="9999"

client_ip="127.0.0.1"
client_port="8888"

queries="70"
inserts="20"
deletions="10"

client_command="java -cp ./_class RMIClient $semaphore_ip $semaphore_port $client_ip $client_port $queries $inserts $deletions && read -s -n 1 -p 'Press any button to continue.'"
server_command="java -cp ./_class RMIServer $semaphore_ip $semaphore_port $server_ip $server_port"
semaphore_command="java -cp ./_class RMISemaphore $semaphore_ip $semaphore_port"

if [ ! -d "./_class" ]
then
	mkdir _class
fi

javac ./*.java -d "./_class" -Xlint:unchecked


# NULL 	INIT ALL
# -2 	SEMAPHORE 	 ONLY
# -1 	SERVER 		 ONLY
# 0		SEMAPHORE 3s SERVER
# 1 	CLIENT  	 ONLY
# 9		CLIENT  	 $SEMAPHORE_PORT $CLIENT_PORT
# 87	SAME CALL TERMINAL SERVER ONLY
# 97	SAME CALL TERMINAL CLIENT ONLY
# 99	SAME CALL TERMINAL CLIENT $SEMAPHORE_PORT $CLIENT_PORT 
if [ $? -eq 0 ]
then
	if [[ $1 -eq "" ]]
	then
		xterm -T "RMISemaphore" -ls -e $semaphore_command &
		sleep 3
		xterm -T "RMIServer" -ls -e $server_command &
		sleep 3
		xterm -T "RMIClient" -ls -e $client_command &


	# INIT SEMAPHORE ONLY
	elif [[ $1 -eq "-2" ]]
	then
		xterm -T "RMISemaphore" -ls -e $semaphore_command &

	# INIT SERVIDOR ONLY
	elif [[ $1 -eq "-1" ]]
	then
		xterm -T "RMIServer" -ls -e $server_command &


	# INIT SERVIDOR AND SEMAPHOREONLY
	elif [[ $1 -eq "0" ]]
	then
		xterm -T "RMISemaphore" -ls -e $semaphore_command &
		sleep 3
		xterm -T "RMIServer" -ls -e $server_command &


	# INIT CLIENT ONLY
	elif [[ $1 -eq "1" ]]
	then
		xterm -T "RMIClient" -ls -e $client_command &
	

	# INIT CLIENT ONLY WITH PARAMS
	elif [[ $1 -eq "87" ]]
	then
		java -cp ./_class RMIServer $semaphore_ip $semaphore_port $server_ip $server_port


	# INIT CLIENT ONLY WITH PARAMS
	elif [[ $1 -eq "9" ]]
	then
		#   														          Port          Port
		xterm -T "RMIClient" -ls -e "java -cp ./_class RMIClient $semaphore_ip $2 $client_ip $3 $queries $inserts $deletions && read -s -n 1 -p 'Press any button to continue.'" &
	
	# INIT CLIENT ONLY WITHOUT NEW TERMINAL
	elif [[ $1 -eq "97" ]]
	then
	java -cp ./_class RMIClient $semaphore_ip $semaphore_port $client_ip $client_port $queries $inserts $deletions


	# INIT CLIENT ONLY WITH PARAMS WITHOUT NEW TERMINAL
	elif [[ $1 -eq "99" ]]
	then 													
    #										     Port          Port
		java -cp ./_class RMIClient $semaphore_ip $2 $client_ip $3 $queries $inserts $deletions
	fi
fi