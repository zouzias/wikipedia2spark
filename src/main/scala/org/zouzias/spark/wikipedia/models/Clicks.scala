package org.zouzias.spark.wikipedia.models

/**
  * Represents a click from one wiki article to another.
  * https://datahub.io/dataset/wikipedia-clickstream
  * https://meta.wikimedia.org/wiki/Research:Wikipedia_clickstream
  *
  * @param prevId Id of the article click originated from if any
  * @param currId Id of the article the click went to
  * @param n Number of clicks
  * @param prevTitle Title of the article click originated from if any
  * @param currTitle Title of the article the click went to
  * @param clickType Type of clicks, see documentation for more information
  */
case class Clicks(prevId: String, currId: String, n: Long, prevTitle: String, currTitle: String, clickType: String)
