if   [  $SERVER_PORT  ];
then
   java -Dserver.port=$SERVER_PORT -jar /usr/local/src/my-tail-based-0.0.1-SNAPSHOT.jar &
else
   java -Dserver.port=8000 -jar /usr/local/src/my-tail-based-0.0.1-SNAPSHOT.jar &
fi
tail -f /usr/local/src/start.sh