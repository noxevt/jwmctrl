#!/bin/sh
JAVA_HOME=/usr/java/jdk1.6.0_45
ANT_HOME=/usr/ant/apache-ant-1.9.0

PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
ant
