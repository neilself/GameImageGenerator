package com.cicero.cardgame

import com.sksamuel.scrimage.ImmutableImage

/**
 * Simple cache for storing images loaded from disk.
 */
class FileImageCache {
  private val imageMap = mutableMapOf<String, ImmutableImage>()

  fun hasImage(filename: String): Boolean {
    return imageMap.containsKey(filename)
  }

  fun getImage(filename: String): ImmutableImage {
    return imageMap[filename] ?: throw IllegalArgumentException("Cache doesn't have key $filename")
  }

  fun addImage(filename: String, image: ImmutableImage) {
    imageMap[filename] = image
  }
}