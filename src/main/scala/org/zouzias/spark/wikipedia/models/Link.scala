package org.zouzias.spark.wikipedia.models

/**
  * Represent a link from one wiki article to another
  *
  * @param pageTitle Title of the current article
  * @param linkTitle Title of the linked article
  * @param row Row the link shows on
  * @param col Column the link shows up on
  */
case class Link(pageTitle: String, linkTitle: String, row: Int, col: Int)
