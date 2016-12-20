#!/bin/sh
#
# Script used to start BlastViewer during development cycles. It requires 
# BlastViewer be compiled using: ant makejar

V_PROVIDER=EBI

# *** Application home
PL_APP_HOME=.
PL_MAIN_CLASS=bzh.plealog.blastviewer.BlastViewer

# *** Java VM
PL_JAVA_VM=$JAVA_HOME/bin/java
PL_JAVA_ARGS="-Xms2048m -Xmx2048m -DV_DEBUG=true "

# *** JARs section
PL_JAR_LIST_TMP=`\ls $PL_APP_HOME/jar/*.jar`
PL_JAR_LIST=`echo $PL_JAR_LIST_TMP | sed 's/ /:/g'`
PL_JAR_LIST2_TMP=`\ls $PL_APP_HOME/distrib/*.jar`
PL_JAR_LIST=${PL_JAR_LIST}:`echo $PL_JAR_LIST2_TMP | sed 's/ /:/g'`

# *** start application
$PL_JAVA_VM $PL_JAVA_ARGS -classpath $PL_JAR_LIST $PL_MAIN_CLASS

