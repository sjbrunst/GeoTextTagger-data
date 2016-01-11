This program streams tweets from Twitter and saves them to Parquet files.

## Prerequisites ##

1. Install [SBT](http://www.scala-sbt.org/).
2. Run the ``build_spark.sh`` script in the root of this repository.
3. After succesfully completing step 2, run the ``CopyJars.sh`` script in this folder.
4. Add your Twitter authentication crendentials to the twitter4j.properties file in this folder (see below).

## Run Instructions ##

Simply run ``./run.sh`` in this folder. Tweets will be saved to ../tweets, but you can change this behaviour in that script.

This program will output one parquet file every 10 minutes. You can change this frequency in src/main/scala/GetTweets.scala.

If you wish to combine the parquet files after producing them, use the ../MergeParquet program.

## Twitter Authentication ##

You need authentication credentials from Twitter to run this.

The following steps describe how to obtain your own credentials:

1. Sign up for a Twitter account
2. Go to [https://apps.twitter.com](https://apps.twitter.com). Sign in with your Twitter account if you are not already signed in.
3. Click the "Create New App" button
4. Fill out the application form and click "Create your Twitter application"
5. On your Application Management page, go to Keys and Access Tokens
6. Click "Create my access token"
7. Copy and paste the following keys into twitter4j.properties file in this folder: Consumer Key (API Key), Consumer Secret (API  Secret), Access Token, and Access Token Secret

As long as this twitter4j.properties file is in the base directory of a Spark Streaming application, Spark will be able to use these credentials to connect to Twitter.

## Note About Spark ##

This project requires a modified version of Spark in order to request geotagged tweets. A copy of the Spark source code with the necessary modifications has been included in ../spark-1.3.1.

To compile this program with the modified Spark, you must first compile Spark (using ``../build_spark.sh``). Then run ``./CopyJars.sh`` from this folder. This will copy the necessary jar files into a lib folder here.

