import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext

object CreateTweetData {

  def main(args: Array[String]) {

    if (args.length != 2) {
      if (args.length < 2) {
        println("ERROR: not enough arguments.")
      } else {
        println("ERROR: too many arguments.")
      }
      println("Arguments: inputFile outputFile")
      System.exit(1)
    }

    val inputFile = args(0)
    val outputFile = args(1)

    val conf = new SparkConf().setAppName("CreateTweetText")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val parquetFile = sqlContext.read.parquet(inputFile)
    val stringRDD = parquetFile.select("tweetid","text","latitude","longitude")
                               .map(t => (t(0).toString + "\t" + t(1).toString.replace('\t',' ').replace('\n',' ') + "\t" + "tweet" + "\t" + t(2).toString + "\t" + t(3).toString))

    stringRDD.saveAsTextFile(outputFile)

    sc.stop()
  }
}
