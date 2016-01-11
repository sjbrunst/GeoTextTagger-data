import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import java.nio.file.{Paths, Files}
import java.io.File

/*
  This program finds parquet files and merges them.
  Usage: folder [deleteInput [parts]]

  folder: The folder in which to search for parquet files

  deleteInput: If true, will delete the original parquet files
    once a merged one is created successfully. Default: false

  parts: The number of file parts to use for the output parquet file.
    Each part should be small enough to easily fit in memory.
      (I've had out of memory errors when a part was 200 MB.)
    However, if parts are too small then that is inefficient for both
      reading and writing.
    You should have at least as many parts as there are processors on your
      machine to allow for maximum parallelization.

  Example: sbt 'run ../tweets false 4'
*/

object Merge {

  // This function returns all complete parquet files in the given folder
  def getParquetFileList(searchFolder: String):Array[String] = {
    return (new File(searchFolder)).listFiles
                                   .filter(_.isDirectory)
                                   .map(_.getName)
                                   .filter(_.endsWith(".parquet"))
                                   .filter(name => 
                Files.exists(Paths.get(searchFolder + name + "/_SUCCESS")))
  }

  def main(args: Array[String]) {

    if ((args.length > 3) || (args.length < 1)) {
      if (args.length < 1) {
        println("ERROR: Too few arguments.")
      } else {
        println("ERROR: Too many arguments.")
      }
      println("Usage: sbt 'run folder [deleteInput [parts]]'")
      println("  folder: Contains the parquet files you wish to combine.")
      println("  deleteInput: Removes the input parquet files if the combined file is created successfully. Default: false")
      println("  parts: Number of file parts in the created parquet file. Default: 4")
      System.exit(1)
    }

    // Folder to search. 
    val inputFolder = args(0) + {if (args(0).endsWith("/")) {""} else {"/"}}

    // default values:
    var deleteInput:Boolean = false
    var parts:Int = 4

    if (args.length > 1) {
      deleteInput = args(1).toBoolean
      if (args.length > 2) {
        parts = args(2).toInt
      }
    }

    // Name of the parquet file to create:
    val outputLocation = inputFolder + "merged" + "-" + System.currentTimeMillis().toString + ".parquet"

    val dirList = getParquetFileList(inputFolder)

    if (dirList.length < 1) {
      println("ERROR: No parquet files found.")
      System.exit(3)
    }

    var conf = new SparkConf().setMaster("local[4]") // Ideally this number
                                                     // should equal the number
                                                     // of processors on this
                                                     // machine.
                              .setAppName("Merge")
    var sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // This RDD combines all the input data:
    var comboRDD = sqlContext.parquetFile(inputFolder+dirList(0))
    if (dirList.length > 1) {
      for (dir <- dirList.slice(1, dirList.length)) {
        comboRDD = comboRDD.unionAll(sqlContext.parquetFile(inputFolder+dir))
      }
    }

    // The "true" below is to shuffle the data so the file part sizes are
    // distributed evenly. Otherwise file parts may grow so large that they
    // don't fit in memory, which will cause future reads of the file to crash.
    comboRDD.repartition(parts).saveAsParquetFile(outputLocation)

    // If the "_SUCCESS" file exists then the parquet file was created
    // successfully.
    if (Files.exists(Paths.get(outputLocation + "/_SUCCESS"))) {
      // Here we can delete the input files since their data exists in the
      // file we just created.
      if (deleteInput) {
        for (dir <- dirList) {
          // We have to delete each file part inside the input parquet files,
          // because Files.delete(...) does not work on non-empty directories.
          for (file<-(new File(inputFolder + dir)).listFiles.map(_.getName)) {
            Files.delete(Paths.get(inputFolder + dir + "/" + file))
          }
          // Now we can delete the directory:
          Files.delete(Paths.get(inputFolder + dir))
        }
      }
    } else {
      println("ERROR: could not write parquet file successfully")
      println("If a partial file was created then you should delete it.")
      sc.stop()
      System.exit(4)
    }

    sc.stop()

  }
}

