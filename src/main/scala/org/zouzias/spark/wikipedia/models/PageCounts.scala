package org.zouzias.spark.wikipedia.models

/**
  * Represents a page counts on a wikpiedia article
  * https://wikitech.wikimedia.org/wiki/Analytics/Data/Pagecounts-all-sites
  *
  * @param project   The project the page view is for (ie: en, en.m, fr, etc.)
  * @param pageTitle The title of the page
  * @param views     The number of views for the page in this project
  */
case class PageCounts(project: String, pageTitle: String, views: Long)
