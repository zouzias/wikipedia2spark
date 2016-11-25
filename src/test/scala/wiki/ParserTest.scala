package wiki

import generic.Spark
import org.scalatest.FunSuite
import com.google.common.io.Resources
import org.zouzias.spark.wikipedia.parse.Parser

class ParserTest extends FunSuite {

  test("testParseInternalLinks") {
    val rawpges = Parser.readWikiDump(Spark.sc, "file://" + Resources.getResource("wikidump.xml").getPath)
    val pages = Parser.parsePages(rawpges)
    assert(Parser.parseInternalLinks(pages.map(_._2)).count() == 7070)
  }

  test("testParsePages") {
    val rawpges = Parser.readWikiDump(Spark.sc, "file://" + Resources.getResource("wikidump.xml").getPath)
    assert(Parser.parsePages(rawpges).count() == 85)
  }

  test("testParseRedirects") {
    val rawpges = Parser.readWikiDump(Spark.sc, "file://" + Resources.getResource("wikidump.xml").getPath)
    val pages = Parser.parsePages(rawpges)
    val redirects = Parser.parseRedirects(pages.map(_._2))
    assert(redirects.count() == 67)
  }


  test("testReadWikiDump") {
    val rdd = Parser.readWikiDump(Spark.sc, "file://" + Resources.getResource("wikidump.xml").getPath)
    rdd.collect().foreach(println)
    assert(rdd.count() == 85)
  }

  test("testReadPageCounts") {
    assert(Parser.readPageCounts(Spark.sc, "file://" + Resources.getResource("pagecounts").getPath).count() == 100)
  }


  test("testReadClickSteam") {
    assert(Parser.readClickSteam(Spark.sc, "file://" + Resources.getResource("clickstream.tsv").getPath).count() == 99)
  }

}
