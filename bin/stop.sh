#!/usr/bin/env bash

PID_FILE=server.pid;

cd ../
java -cp web-server-1.0-SNAPSHOT-jar-with-dependencies.jar  com.dzytsiuk.webserver.ShutDown
if [[  -f ${PID_FILE} ]]; then
    pid=$(<${PID_FILE})
    #if process is still not finished killing it
    kill -9 ${pid}
    rm ${PID_FILE}
fi
echo server is shutdown