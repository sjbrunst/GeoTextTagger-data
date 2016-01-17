# CreateTweetText

This program creates a tab-separated file with one tweet per line.

## Setup: Getting Spark

Note: This section is identical for the CreateTweetText, CreateTweetTextByUser, and CreateWikiText programs. This section only needs to be done once, and can be reused by all three.

These programs were written for Spark 1.4.1. Download a compiled version of Spark 1.4.1 into the root directory of this repository using `wget http://d3kbcqa49mib13.cloudfront.net/spark-1.4.1-bin-hadoop1.tgz` then unzip the file using `tar -xf ./spark-1.4.1-bin-hadoop1.tgz` Alternatively, you can download Spark from http://spark.apache.org/downloads.html.

Note that this is different than the version of Spark that StreamFromTwitter is currently written for. (We tried upgrading StreamFromTwitter to use verison 1.4.1, but the resulting program was unstable.)

## Running

First, compile this program with `sbt package`.

Assuming you have downloaded Spark 1.4.1 into the root of this repository, you can then run this program using

`../spark-1.4.1-bin-hadoop1/bin/spark-submit --class CreateTweetData --master local[4] ./target/scala-2.10/createtweettext_2.10-1.0.jar input-file output-path`

where `input-file` is the path to a parquet file containing the tweets you wish to format (produced by the StreamFromTwitter and MergeParquet programs) and `output-path` is the path to the file you wish to create.

Note that the output will be divided into many text files. To combine them into a single file, you can run

`cat output-path/part-* > output-file`

Output will be in the same format as the data files in ../Data. See the Readme file there for details.
