package org.zouzias.spark.wikipedia.models

/**
  * Represents a redirect from one wiki article title to another
  *
  * @param pageTitle Title of the current article
  * @param redirectTitle Title of the article being redirected to
  */
case class Redirect(pageTitle: String, redirectTitle: String)
