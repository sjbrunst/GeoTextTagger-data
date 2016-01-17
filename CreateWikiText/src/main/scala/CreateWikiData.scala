import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext

object CreateWikiData {

  val tagFile = "../enwiki-20150602-geo_tags.txt"
  val textFile = "../extracted/all.txt"
  val outputFile = "wiki_primary.txt"

  case class Tag(
    page_id: Long,
    globe: String,
    primary: Boolean,
    lat: Float,
    lon: Float,
    tag_type: String,
    name: String
  )

  case class Article(
    page_id: Long,
    title: String,
    text: String,
    tags: Array[Tag]
  )

  /* Columns in the wiki file:
  `gt_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `gt_page_id` int(10) unsigned NOT NULL,
  `gt_globe` varbinary(32) NOT NULL,
  `gt_primary` tinyint(1) NOT NULL,
  `gt_lat` float NOT NULL,
  `gt_lon` float NOT NULL,
  `gt_dim` int(11) DEFAULT NULL,
  `gt_type` varbinary(32) DEFAULT NULL,
  `gt_name` varbinary(255) DEFAULT NULL,
  `gt_country` binary(2) DEFAULT NULL,
  `gt_region` varbinary(3) DEFAULT NULL,
  */

  def lineToTag(line: String) : Tag = {
    val lineSplit = line.split(',')
    return new Tag(
      lineSplit(1).toLong,
      lineSplit(2),
      (lineSplit(3) == "1"),
      lineSplit(4).toFloat,
      lineSplit(5).toFloat,
      lineSplit(7),
      lineSplit(8)
    )
  }

  def lineToTags(line: String) : Array[Tag] = {
    return line.stripPrefix("INSERT INTO `geo_tags` VALUES (")
               .stripSuffix(")")
               .split("\\),\\(")
               .map(lineToTag(_))
  }

  def lineToArticle(line: String) : Article = {
    val idStart = line.indexOfSlice("id=\"") + 4
    val idEnd = line.indexOf('"', idStart)
    val titleStart = line.indexOfSlice("title=\"", idEnd) + 7
    val titleEnd = line.indexOf('"', titleStart)
    val textStart = line.indexOf('>', titleEnd + 1) + 1
    val textEnd = line.lastIndexOfSlice("</doc>")
    return new Article(line.slice(idStart,idEnd).toLong,
                       line.slice(titleStart,titleEnd),
                       line.slice(textStart,textEnd).trim(),
                       Array())
  }

  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("CreateWikiText")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val tags = sc.textFile(tagFile)
                 .filter(_.startsWith("INSERT INTO `geo_tags` VALUES ("))
                 .flatMap(lineToTags(_))
                 .filter(_.primary)
    val text = sc.textFile(textFile).map(lineToArticle(_)).cache()

    val tagsKV = tags.map(t => (t.page_id, t)).cache()

    val joined = text.map(a => (a.page_id, a))
                     .join(tagsKV)
                     .values
                     .map{case (a, t) => (a.page_id.toString + "\t" + a.text.replace('\t',' ') + "\t" + t.tag_type + "\t" + t.lat.toString + "\t" + t.lon.toString)}

    joined.saveAsTextFile(outputFile)

    sc.stop()
  }
}
