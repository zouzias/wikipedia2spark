package org.zouzias.spark.wikipedia.parse

import java.io.ByteArrayInputStream

import info.bliki.wiki.dump.{IArticleFilter, Siteinfo, WikiArticle, WikiXMLParser}
import info.bliki.wiki.filter.WikipediaParser
import info.bliki.wiki.model.WikiModel
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.htmlcleaner.HtmlCleaner
import org.xml.sax.SAXException
import org.zouzias.spark.wikipedia.input.WikiInputFormat
import org.zouzias.spark.wikipedia.models._

object Parser {

  /**
    * Helper class for parsing wiki XML, parsed pages are set in wrappedPage
    *
    */
  class SetterArticleFilter(val wrappedPage: WrappedPage) extends IArticleFilter {
    @throws(classOf[SAXException])
    def process(page: WikiArticle, siteinfo: Siteinfo)  {
      wrappedPage.page = page
    }
  }

  /**
    * Read page counts data from a directory
    * https://wikitech.wikimedia.org/wiki/Analytics/Data/Pagecounts-all-sites
    */
  def readPageCounts(sc: SparkContext, path: String): RDD[PageCounts] = {
    val rdd = sc.textFile(path + "/*")
    rdd.map(_.split(" ")).map(l => PageCounts(
      l(0),
      StringEscapeUtils.unescapeHtml4(l(1)),
      l(2).toLong
    )
    )
  }

  /**
    * Reads click stream data from a file
    * https://datahub.io/dataset/wikipedia-clickstream
    * https://meta.wikimedia.org/wiki/Research:Wikipedia_clickstream
    */
  def readClickSteam(sc: SparkContext, file: String) : RDD[Clicks] = {
    val rdd = sc.textFile(file)
    rdd.zipWithIndex().filter(_._2 != 0).map(_._1)
      .repartition(10)
      .map(_.split('\t'))
      .map(l => Clicks(
        l(0),
        l(1),
        l(2).toLong,
        l(3).replace("_"," "), //Click stream uses _ for spaces while the dump parsing uses actual spaces
        l(4).replace("_"," "), //Click stream uses _ for spaces while the dump parsing uses actual spaces
        l(5))
      )
  }

  /**
    * Reads a wiki dump xml file, returning a single row for each <page>...</page>
    * https://en.wikipedia.org/wiki/Wikipedia:Database_download
    * https://meta.wikimedia.org/wiki/Data_dump_torrents#enwiki
    */
  def readWikiDump(sc: SparkContext, file: String) : RDD[(Long, String)] = {
    val rdd = sc.hadoopFile[LongWritable, Text, WikiInputFormat](file)
    rdd.map{case (k,v) => (k.get(), new String(v.copyBytes()))}.repartition(100)
  }

  /**
    * Parses the raw page text produced by readWikiDump into Page objects
    */
  def parsePages(rdd: RDD[(Long, String)]): RDD[(Long, Page)] = {
    rdd.mapValues{
      text => {
        val wrappedPage = new WrappedPage
        //The parser occasionally exceptions out, we ignore these
        try {
          val parser = new WikiXMLParser(new ByteArrayInputStream(text.getBytes), new SetterArticleFilter(wrappedPage))
          parser.parse()
        } catch {
          case e: Exception =>
        }
        val page = wrappedPage.page
        if (page.getText != null && page.getTitle != null
          && page.getId != null && page.getRevisionId != null
          && page.getTimeStamp != null) {
          Some(Page(page.getTitle, page.getText, page.isCategory, page.isFile, page.isTemplate))
        } else {
          None
        }
      }
    }.filter(_._2.isDefined).mapValues(_.get)
  }

  /**
    * Parses redirects out of the Page objects
    */
  def parseRedirects(rdd: RDD[Page]): RDD[Redirect] = {
    rdd.map {
      page =>
        val redirect =
          if (page.text != null && !page.isCategory && !page.isFile && !page.isTemplate) {
            val r =  WikipediaParser.parseRedirect(page.text, new WikiModel("", ""))
            if (r == null) {
              None
            } else {
              Some(Redirect(page.title,r))
            }
          } else {
            None
          }
        redirect
    }.filter(_.isDefined).map(_.get)
  }

  /**
    * Parses internal article links from a Page object, filtering out links that aren't to articles
   */
  def parseInternalLinks(rdd: RDD[Page]): RDD[Link] = {
    rdd.flatMap {
      page =>
        if (page.text != null) {
          try {
            val html = WikiModel.toHtml(page.text)
            val cleaner = new HtmlCleaner
            val rootNode = cleaner.clean(html)
            val elements = rootNode.getElementsByName("a", true)
            val out = for (
              elem <- elements;
              classType = elem.getAttributeByName("class");
              title = elem.getAttributeByName("title")
              if (
                title != null
                  && !title.startsWith("User:") && !title.startsWith("User talk:")
                  && (classType == null || !classType.contains("external"))
                )
            ) yield {
              Link(page.title, StringEscapeUtils.unescapeHtml4(title), elem.getRow, elem.getCol)
            }
            out.toList
          }  catch {
            case e: Exception => Nil
          }
        } else {
          Nil
        }
    }
  }
}
