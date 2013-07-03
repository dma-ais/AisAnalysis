#!/bin/bash

SCRIPTPATH=`dirname $0`
cd $SCRIPTPATH

if [ -z $2 ]
then
	CONFFILE=coverage.xml
else
	CONFFILE=$2
fi

PROCNAME="dk.dma.ais.analysis.coverage.AisCoverageDaemon -file $CONFFILE"

stop () {
	# Find pid
	PID=`./getpid.pl "$PROCNAME"`
	if [ -z $PID ]; then
		echo "AisCoverageDaemon not running"
		exit 1
	fi
	echo "Stopping AisCoverageDaemon"
	kill $PID
    exit 0
}

case "$1" in
start)
	PID=`./getpid.pl "$PROCNAME"`
	if [ ! -z $PID ]; then
		echo "AisCoverageDaemon already running"
		exit 1
	fi
    echo "Starting AisCoverageDaemon"
    ./coverage.sh -file $CONFFILE > /dev/null 2>&1 &
    ;;
stop)
    stop
    ;;
restart)
    $0 stop
    sleep 1
    $0 start
    ;;
*)
    echo "Usage: $0 (start|stop|restart|help) [conffile]"
esac
