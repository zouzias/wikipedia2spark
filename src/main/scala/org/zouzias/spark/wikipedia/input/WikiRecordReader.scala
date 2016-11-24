package org.zouzias.spark.wikipedia.input

/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import java.io.{IOException, InputStream}

import org.apache.commons.logging.{Log, LogFactory}
import org.apache.hadoop.classification.{InterfaceAudience, InterfaceStability}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FileSystem, Path, Seekable}
import org.apache.hadoop.io.compress.{CodecPool, CompressionCodec, CompressionCodecFactory, Decompressor}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.{FileSplit, RecordReader}

/**
  * Treats keys as offset in file and value as <page>...</page>.
  */
@InterfaceAudience.LimitedPrivate(Array("MapReduce", "Pig"))
@InterfaceStability.Unstable
object WikiRecordReader {
  private val LOG: Log = LogFactory.getLog(classOf[WikiRecordReader].getName)
}

@InterfaceAudience.LimitedPrivate(Array("MapReduce", "Pig"))
@InterfaceStability.Unstable
class WikiRecordReader extends RecordReader[LongWritable, Text] {
  private var start: Long = 0L
  private var pos: Long = 0L
  private var end: Long = 0L
  private var in: InputStream = null
  private final var filePosition: Seekable = null
  var maxLineLength: Int = 0
  private var codec: CompressionCodec = null
  private var decompressor: Decompressor = null
  private var wikiReader: WikiReader = null

  @throws(classOf[IOException])
  def this(job: Configuration, split: FileSplit) {
    this()
    start = split.getStart
    end = start + split.getLength
    val file: Path = split.getPath
    val compressionCodecs: CompressionCodecFactory = new CompressionCodecFactory(job)
    codec = compressionCodecs.getCodec(file)
    val fs: FileSystem = file.getFileSystem(job)
    val fileIn: FSDataInputStream = fs.open(file)
    if (isCompressedInput) {
      decompressor = CodecPool.getDecompressor(codec)
      in = codec.createInputStream(fileIn, decompressor)
      filePosition = fileIn
    }
    else {
      fileIn.seek(start)
      in = fileIn
      filePosition = fileIn
    }
    wikiReader = new WikiReader(in)
    this.pos = start
  }

  def createKey: LongWritable = {
    new LongWritable
  }

  def createValue: Text = {
    new Text
  }

  private def isCompressedInput: Boolean = {
    codec != null
  }

  @throws(classOf[IOException])
  private def getFilePosition: Long = {
    filePosition.getPos
  }

  /** Read a line. */
  @throws(classOf[IOException])
  def next(key: LongWritable, value: Text): Boolean = {
    if (getFilePosition <= end) {
      key.set(getFilePosition)
      val page: String = wikiReader.read()
      if (page == "") {
        return false
      }
      else {
        value.set(page)
        return true
      }
    }
    false
  }

  /**
    * Get the progress within the split
    */
  @throws(classOf[IOException])
  def getProgress: Float = {
    0.0f
  }

  @throws(classOf[IOException])
  def getPos: Long = {
    pos
  }

  @throws(classOf[IOException])
  def close() {
    try {
      if (in != null) {
        in.close()
      }
    } finally {
      if (decompressor != null) {
        CodecPool.returnDecompressor(decompressor)
      }
    }
  }
}
