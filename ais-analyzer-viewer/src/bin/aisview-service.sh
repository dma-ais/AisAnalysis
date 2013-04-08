#!/bin/bash

SCRIPTPATH=`dirname $0`
cd $SCRIPTPATH

if [ -z $2 ]
then
	CONFFILE=aisview.xml
else
	CONFFILE=$2
fi

PROCNAME="dk.dma.ais.analysis.viewer.AisViewDaemon -file $CONFFILE"

stop () {
	# Find pid
	PID=`./getpid.pl "$PROCNAME"`
	if [ -z $PID ]; then
		echo "AisViewDaemon not running"
		exit 1
	fi
	echo "Stopping AisViewDaemon"
	kill $PID
    exit 0
}

case "$1" in
start)
	PID=`./getpid.pl "$PROCNAME"`
	if [ ! -z $PID ]; then
		echo "AisViewDaemon already running"
		exit 1
	fi
    echo "Starting AisViewDaemon"
    ./aisview.sh -file $CONFFILE > /dev/null 2>&1 &
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
