#!/usr/bin/env bash
PID_FILE=server.pid;
cd ../
if [[  -f ${PID_FILE} ]]; then
    echo "Server is already launched";
    exit;
fi
echo server is launching
java -jar web-server-1.0-SNAPSHOT-jar-with-dependencies.jar &
echo $! > ${PID_FILE}
