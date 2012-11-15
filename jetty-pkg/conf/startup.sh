#!/usr/bin/env bash

VERSION=1.0.0-SNAPSHOT

PRGDIR=`dirname "$0"`
HDFSUI_HOME=`cd "$PRGDIR/.." ; pwd`

base_dir=$(dirname $0)/..

for file in $base_dir/lib/*.jar;
do
    CLASSPATH=$CLASSPATH:$file
  done

if [ -z $HDFSUI_OPTS ]; then
  HDFSUI_OPTS="-Xmx1G -server -Dcom.sun.management.jmxremote"
fi

java -Dhdfsui_home=$HDFSUI_HOME -Dconfig=$HDFSUI_HOME/conf/jetty.properties -Dlog4j.configuration=file://$HDFSUI_HOME/conf/log4j.xml $HDFSUI_OPTS -cp $CLASSPATH -jar $HDFSUI_HOME/lib/simple-web-hdfs-$VERSION.jar
