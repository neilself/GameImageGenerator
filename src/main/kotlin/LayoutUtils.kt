package com.cicero.cardgame

import com.cicero.cardgame.resources.GameResources
import com.cicero.cardgame.resources.MainResources.Companion.OUTPUT_IMAGES_DIRECTORY
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.awt.Color
import java.awt.Graphics2D
import java.io.File

/**
 * A class with some useful utility functions for laying things out.
 */
object LayoutUtils {

  fun loadDataTableFromFile(keyForFilename: String, resources: GameResources): DataTable {
    val lines = File(resources.getString(keyForFilename)).bufferedReader().readLines()
    val listOfLists = mutableListOf<List<String>>()
    for (i in lines.indices) {
      val line = lines[i]
      val sections = smartSplitCsvLine(line)
      listOfLists.add(sections)
    }

    return DataTable(listOfLists)
  }

  fun writeImageOut(image: ImmutableImage, filename: String, resources: GameResources) {
    image.output(PngWriter.MinCompression, File(
      "${resources.getString(OUTPUT_IMAGES_DIRECTORY.toLowerCase())}$filename.png"))
  }

  fun drawGraphicsBox(image: ImmutableImage, drawBoundingBoxes: Boolean) {
    val graphics = image.awt().createGraphics() ?: throw IllegalStateException("Unable to instantiate " +
            "graphics for an image")
    drawGraphicsBox(graphics, image.width, image.height, drawBoundingBoxes)
  }

  /**
   * Draws a magenta box, for debugging purposes.
   */
  fun drawGraphicsBox(graphics: Graphics2D, width: Int, height: Int, drawBoundingBoxes: Boolean) {
    if (!drawBoundingBoxes) {
      return
    }

    val oldColor = graphics.color
    graphics.color = Color.magenta
    graphics.drawRect(0, 0, width - 1, height - 1)
    graphics.color = oldColor
  }

  fun smartSplitCsvLine(line: String): List<String> {
    var inQuotation = false
    val sections = mutableListOf<String>()
    var i = 0
    while (i < line.length) {
      val builder = StringBuilder()
      while (i < line.length) {
        if (line[i] == '\"') {
          inQuotation = !inQuotation
          i++
        } else if (line[i] == ',' && !inQuotation) {
          i++
          break
        } else {
          builder.append(line[i])
          i++
        }
      }
      sections.add(builder.toString())
    }

    return sections
  }

}