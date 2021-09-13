#!/bin/bash

#			 								Server-IP Port  Client-IP C-Port
client_command="java -cp ./_class RMIClient 127.0.0.1 52369 127.0.0.1 8888 && read -s -n 1 -p 'Press any button to continue.'"
server_command="java -cp ./_class RMIServer 127.0.0.1 52369"

echo "Compilling"

if [ ! -d "./_class" ]
then
	mkdir _class
fi

javac ./*.java -d "./_class"

if [ $? -eq 0 ]
then
	# INIT BOTH
	if [[ $1 -eq "" ]]
	then
		xterm -T "RMIServer" -ls -e $server_command &
		sleep 5
		xterm -T "RMIClient" -ls -e $client_command &

	# INIT SERVIDOR ONLY
	elif [[ $1 -eq "0" ]]
	then
		xterm -T "RMIServer" -ls -e $server_command &

	# INIT CLIENT ONLY
	elif [[ $1 -eq "1" ]]
	then
		xterm -T "RMIClient" -ls -e $client_command &
	
	# INIT CLIENT ONLY WITH PARAMS
	elif [[ $1 -eq "9" ]]
	then
		# 														          Port         Port
		xterm -T "RMIClient" -ls -e "java -cp ./_class RMIClient 127.0.0.1 $2 127.0.0.1 $3 && read -s -n 1 -p 'Press any button to continue.'" &

	# INIT CLIENT ONLY WITH PARAMS ON WITHOUT THE TERMINAL
	elif [[ $1 -eq "99" ]]
	then 													
    #										 Port         Port
		java -cp ./_class RMIClient 127.0.0.1 $2 127.0.0.1 $3
	fi
fi

echo "Done"