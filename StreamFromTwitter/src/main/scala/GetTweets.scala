import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.twitter._

object GetTweets {

  val locations = Seq(BoundingBox(-140,24,-52,83), // US and Canada
                      BoundingBox(3.358,50.7504,7.2275,53.556), // Netherlands
                      BoundingBox(112.92,-44.15,159.26,-9.22))  // Australia

  case class Tweet(text: String,
                   tweetid: Long,
                   latitude: Double,
                   longitude: Double,
                   userId: Long,
                   userName: String,
                   userTwitterName: String,
                   userLocation: String,
                   created_at: Long)

  def main(args: Array[String]) {

    if (args.length != 1) {
      if (args.length < 1) {
        println("ERROR: not enough arguments.")
      } else {
        println("ERROR: too many arguments.")
      }
      println("Usage: sbt 'run outputFolder'")
      System.exit(1)
    }

    val outputFolder = args(0) + {if (args(0).endsWith("/")) {""} else {"/"}}
    println("Writing tweets to " + outputFolder)

    var conf = new SparkConf().setMaster("local[4]")
                              .setAppName("TweetCollection")
                              
    var sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    var ssc = new StreamingContext(sc, Seconds(60*10)) // change this to change the frequency of output
    val tweetStream = TwitterUtils.createStream(ssc, None, 0, Nil, Nil, locations)

    // Only keep tweets with an actual geolocation and write them to a parquet file.
    // (Some "geotagged" tweets only have a bounding box for the user's home city.)
    tweetStream.filter(tweet => tweet.getGeoLocation() != null)
               .foreachRDD(streamedRDD => {
                 val tweetRDD = streamedRDD.map(tweet => {
                     val location = tweet.getGeoLocation()
                     val user = tweet.getUser()
                     // This is what is returned in the map function:
                     Tweet(tweet.getText(),
                           tweet.getId(),
                           location.getLatitude(),
                           location.getLongitude(),
                           user.getId(),
                           user.getName(),
                           user.getScreenName(),
                           user.getLocation(),
                           // Twitter gives the time in milliseconds, but
                           // rounded to the nearest second. Convert to seconds
                           // here:
                           tweet.getCreatedAt().getTime()/1000)
                     })
                 val tweetCount = tweetRDD.count()
                 // Don't bother writing the file if there are no tweets:
                 if (tweetCount > 0) {
                   val currentTime = System.currentTimeMillis().toString
                   println("Writing " + tweetCount + " tweets at time " + currentTime)
                   // The repartition below ensures that the RDD has 4 partitions,
                   // meaning it will be written as a parquet file with 4 parts.
                   tweetRDD.repartition(4).toDF().saveAsParquetFile(outputFolder +
                       "10MINUTES-" + currentTime + ".parquet")
                 } else {
                   println("WARNING: no tweets written at " +
                           System.currentTimeMillis().toString())
                 }
               })

    ssc.start()
    ssc.awaitTermination()

    ssc.stop(true,true)
    
    sc.stop()
  }
}

