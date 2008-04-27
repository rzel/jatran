#!/bin/sh

LIB_DIR="./lib"
CP=".:./dist/jatran.jar"

for i in `ls $LIB_DIR/*.jar`;
do
	CP="$CP:$i"
done

java -cp $CP jatran.main.Main "$@"
