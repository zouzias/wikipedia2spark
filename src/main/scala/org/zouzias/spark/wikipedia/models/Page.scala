package org.zouzias.spark.wikipedia.models

/**
  * Represents a parsed Wikipedia page from the Wikipedia XML dump
  *
  * https://en.wikipedia.org/wiki/Wikipedia:Database_download
  * https://meta.wikimedia.org/wiki/Data_dump_torrents#enwiki
  *
  * @param title Title of the current page
  * @param text Text of the current page including markup
  * @param isCategory Is the page a category page, not perfectly accurate
  * @param isFile Is the page a file page, not perfectly accurate
  * @param isTemplate Is the page a template page, not perfectly accurate
  */
case class Page(title: String, text: String, isCategory: Boolean , isFile: Boolean, isTemplate: Boolean)

