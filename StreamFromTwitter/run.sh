#!/bin/bash

mkdir -p ../tweets

# so far I haven't figured out how to get spark-submit working with my modified Spark
sbt "run $PWD/../tweets"
