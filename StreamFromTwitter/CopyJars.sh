#!/bin/bash

mkdir -p ./lib

sparkfolder=../spark-1.3.1

cp $sparkfolder/assembly/target/scala-2.10/spark-assembly-*.jar ./lib/
cp $sparkfolder/core/target/spark-core*.jar ./lib/
cp $sparkfolder/streaming/target/spark-streaming*.jar ./lib/
cp $sparkfolder/external/twitter/target/spark-streaming-twitter*.jar ./lib/
cp $sparkfolder/repl/target/spark-repl*.jar ./lib/
