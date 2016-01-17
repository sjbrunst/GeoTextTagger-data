# CreateWikiText

This program creates a tab-separated file with one geotagged Wikipedia article per line.

## Setup: Getting Spark

Note: This section is identical for the CreateTweetText, CreateTweetTextByUser, and CreateWikiText programs. This section only needs to be done once, and can be reused by all three.

These programs were written for Spark 1.4.1. Download a compiled version of Spark 1.4.1 into the root directory of this repository using `wget http://d3kbcqa49mib13.cloudfront.net/spark-1.4.1-bin-hadoop1.tgz` then unzip the file using `tar -xf ./spark-1.4.1-bin-hadoop1.tgz` Alternatively, you can download Spark from http://spark.apache.org/downloads.html.

## Running

This program is not set up to take command line arguments, so the paths to your input and output files must be hardcoded to the three paths in src/main/scala/CreateWikiData. They are:

* `tagFile`: path to the unzipped geo\_tags file from Wikipedia
* `textFile`: path to the extracted Wikipedia articles file created from the Formatting Wikipedia Data section of this repositories main Readme file.
* `outputFile`: path to the file this program will output

Once you have set those paths to the desired locations, compile this program with `sbt package`.

Assuming you have downloaded Spark 1.4.1 into the root of this repository, you can then run this program using

`../spark-1.4.1-bin-hadoop1/bin/spark-submit --class CreateWikiData -- master local[4] ./target/scala-2.10/createwikidata_2.10-1.0.jar`

Note that the output will be divided into many text files. To combine them into a single file, you can run `cat outputFile/part-* > newOutputFile`
