package org.zouzias.spark.wikipedia.models

import info.bliki.wiki.dump.WikiArticle

/**
  * A helper class that allows for a WikiArticle to be serialized and also pulled from the XML parser
  *
  * @param page The WikiArticle that is being wrapped
  */
case class WrappedPage(var page: WikiArticle = new WikiArticle)
