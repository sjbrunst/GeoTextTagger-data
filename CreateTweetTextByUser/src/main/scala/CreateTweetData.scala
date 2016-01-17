import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext

object CreateTweetData {

  def main(args: Array[String]) {

    if (args.length < 3) {
      println("ERROR: not enough arguments.")
      println("Arguments: minTweetsPerUser outputFile inputFile ...")
      System.exit(1)
    }

    val minTweetsPerUser = args(0).toInt
    val outputFile = args(1)
    val inputFiles = args.slice(2, args.length)

    val conf = new SparkConf().setAppName("CreateTweetText")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    var allTweets = sqlContext.read.parquet(inputFiles(0))
    if (inputFiles.length > 1) {
      for (filename <- inputFiles.slice(1, inputFiles.length)) {
        allTweets = allTweets.unionAll(sqlContext.read.parquet(filename))
      }
    }
    val stringRDD = allTweets.select("userId","text","latitude","longitude")
                               .map(t => (t(0), (1,
                                                 t(1).toString.replace('\t',' ').replace('\n',' '),
                                                 t(2).asInstanceOf[Double],
                                                 t(3).asInstanceOf[Double])))
                               .reduceByKey((a,b) => (a._1 + b._1,
                                                      a._2 + " " + b._2,
                                                      a._3 + b._3,
                                                      a._4 + b._4))
                               .values
                               .filter(_._1 >= minTweetsPerUser)
                               .map(ctll => (ctll._1, ctll._2, ctll._3 / ctll._1, ctll._4 / ctll._1))
                               .map(ctll => (ctll._1.toString + "\t" + ctll._2.toString + "\t" + "tweet" + "\t" + ctll._3.toString + "\t" + ctll._4.toString))

    stringRDD.saveAsTextFile(outputFile)

    sc.stop()
  }
}
