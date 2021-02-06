package com.cicero.cardgame

import com.cicero.cardgame.resources.GameResources
import com.sksamuel.scrimage.ImmutableImage
import com.github.keelar.exprk.Expressions
import java.io.File
import java.math.RoundingMode

/**
 * This class represents the second phase of laying things out. Here, the 'raw' layout elements that were simply parsed
 * yaml, are converted into layout elements with meaningful values. For example, an image property that indirectly
 * refers to some image filename, will have that image loaded into the LayoutElement. A padding property written as
 * the string name of a constant, will have that constant resolved to a number (and have arithmetic applied, if any is
 * present in the property).
 */
data class LayoutElement(val id: String,
                         val optional: Boolean?,
                         var width: Int,
                         var height: Int,
                         val alignVertical: VerticalAlignment,
                         val alignHorizontal: HorizontalAlignment,
                         val leftMargin: Int,
                         val rightMargin: Int,
                         val topMargin: Int,
                         val bottomMargin: Int,
                         val leftPadding: Int,
                         val rightPadding: Int,
                         val topPadding: Int,
                         val bottomPadding: Int,
                         val image: ImmutableImage?,
                         val text: String?,
                         val fontName: String? = null,
                         val fontSize: Int,
                         val maxTextRowCount: Int,
                         val children: List<LayoutElement>? = null)

fun createFromRawLayoutElement(resources: GameResources, cache: FileImageCache, textMap: Map<String, String>,
                               input: RawLayoutElement): LayoutElement {
  val id = input.id

  // SECTION: optional
  val optional = getOptionalValue(textMap, input)

  // SECTION: Resolve image(s)
  val imageText = textMap[input.image] ?: input.image
  var image = loadImageFromFilename(cache, resources, imageText)

  val imageListText = if (input.image_list != null) input.image_list.split(",") else null
  val image_list = if (imageListText == null) null
    else mutableListOf<ImmutableImage>()
  if (image_list != null && imageListText != null) {
    for (text in imageListText) {
      val img = loadImageFromFilename(cache, resources, text) ?: throw IllegalStateException("Couldn't load image " +
              "file for $text")
      image_list.add(img)
    }
  }

  val image_count = if (input.image_count != null) input.image_count.split(",") else null

  if ((image_list != null).xor(image_count != null)) {
    throw IllegalStateException("image_list and image_count must be non-null or null together")
  }
  if (image != null && image_list != null && image_count != null) {
    throw IllegalStateException("Either image may non-null, OR image_list + image_count may be non-null")
  }

  if (image_list != null && image_count != null) {
    if (image_list.size != image_count.size) {
      throw IllegalStateException("image_list and image_count must be have the same length")
    }
    val countList = mutableListOf<Int>()
    var imageHeight = 0
    var imageWidth = 0
    for (i in image_list.indices) {
      val countStr = textMap[image_count[i]] ?: throw IllegalStateException("Unable to find count in textMap for " +
              "image_list: ${image_count[i]}")
      val count = countStr.toIntOrNull() ?: throw IllegalStateException("Unable to convert count string to integer")
      countList.add(count)

      imageWidth += (count * image_list[i].width)
      imageHeight = image_list[i].height
    }
    var overallImage = ImmutableImage.create(imageWidth, imageHeight)
    var currentX = 0
    for (i in image_list.indices) {
      for (j in 0 until countList[i]) {
        overallImage = overallImage.overlay(image_list[i], currentX, 0)
        currentX += image_list[i].width
      }
    }
    image = overallImage
  }

  if (input.flip_vertical != null) {
    if (image == null) {
      throw IllegalStateException("No image to flip_vertical!")
    }
    if (input.flip_vertical.toBoolean()) {
      image = image.flipY()
    }
  }

  // SECTION: Resolve vertical and horizontal alignments
  val align_vertical = resolveVerticalAlignment(input.align_vertical)
  val align_horizontal = resolveHorizontalAlignment(input.align_horizontal)

  // SECTION: Resolve margins
  val horizontal_margin = resolveIntField(resources, input.horizontal_margin) ?: 0
  var left_margin = resolveIntField(resources, input.left_margin) ?: 0
  var right_margin = resolveIntField(resources, input.right_margin) ?: 0
  if (horizontal_margin != 0) {
    if (left_margin != 0 || right_margin != 0) {
      throw IllegalStateException("Can't use both horizontal_margin with either left_margin or right_margin")
    }
    left_margin = horizontal_margin
    right_margin = horizontal_margin
  }

  val vertical_margin = resolveIntField(resources, input.vertical_margin) ?: 0
  var top_margin = resolveIntField(resources, input.top_margin) ?: 0
  var bottom_margin = resolveIntField(resources, input.bottom_margin) ?: 0
  if (vertical_margin != 0) {
    if (top_margin != 0 || bottom_margin != 0) {
      throw IllegalStateException("Can't use both vertical_margin with either top_margin or bottom_margin")
    }
    top_margin = vertical_margin
    bottom_margin = vertical_margin
  }

  // SECTION: Resolve paddings
  val horizontal_padding = resolveIntField(resources, input.horizontal_padding) ?: 0
  var left_padding = resolveIntField(resources, input.left_padding) ?: 0
  var right_padding = resolveIntField(resources, input.right_padding) ?: 0
  if (horizontal_padding != 0) {
    if (left_padding != 0 || right_padding != 0) {
      throw IllegalStateException("Can't use both horizontal_padding with either left_padding or right_padding")
    }
    left_padding = horizontal_padding
    right_padding = horizontal_padding
  }

  val vertical_padding = resolveIntField(resources, input.vertical_padding) ?: 0
  var top_padding = resolveIntField(resources, input.top_padding) ?: 0
  var bottom_padding = resolveIntField(resources, input.bottom_padding) ?: 0
  if (vertical_padding != 0) {
    if (top_padding != 0 || bottom_padding != 0) {
      throw IllegalStateException("Can't use both vertical_padding with either top_padding or bottom_padding")
    }
    top_padding = vertical_padding
    bottom_padding = vertical_padding
  }

  // SECTION: Resolve size
  var width = resolveIntField(resources, input.width) ?: 0
  if (width == SpecialIntProperty.USE_IMAGE_WIDTH.prop) {
    width = image?.width ?: 0
  }
  var height = resolveIntField(resources, input.height) ?: 0
  if (height == SpecialIntProperty.USE_IMAGE_HEIGHT.prop) {
    height = image?.height ?: 0
  }

  // SECTION: Resolve text
  val text = if (input.text != null) {
    if (textMap.containsKey(input.text)) {
      textMap[input.text]
    } else {
      input.text
    }
  } else {
    null
  }
  val font_name = resolveStringField(resources, input.font_name)
  val font_size = resolveIntField(resources, input.font_size) ?: 0
  val max_text_row_count = resolveIntField(resources, input.max_text_row_count) ?: Int.MAX_VALUE

  // SECTION: Recurse to children
  val children = if (input.children == null) null
                 else mutableListOf<LayoutElement>()
  if (input.children != null && children != null) {
    for (child in input.children) {
      val childElement = createFromRawLayoutElement(resources, cache, textMap, child)
      if (childElement.optional == false) {
        continue
      }
      if (childElement.width == SpecialIntProperty.MATCH_PARENT.prop) {
        childElement.width = width -
                (left_padding + right_padding + childElement.leftMargin + childElement.rightMargin)
      }
      if (childElement.height == SpecialIntProperty.MATCH_PARENT.prop) {
        childElement.height = height -
                (top_padding + bottom_padding + childElement.topMargin + childElement.bottomMargin)
      }
      children.add(childElement)
    }
  }

  return LayoutElement(
    id,
    optional,
    width,
    height,
    align_vertical,
    align_horizontal,
    left_margin,
    right_margin,
    top_margin,
    bottom_margin,
    left_padding,
    right_padding,
    top_padding,
    bottom_padding,
    image,
    text,
    font_name,
    font_size,
    max_text_row_count,
    children
  )
}

/**
 * Many utility functions follow:
 */

fun getOptionalValue(textMap: Map<String, String>, input: RawLayoutElement): Boolean? {
  if (input.optional == null) {
    return null
  }
  val list = input.optional.split("@")
  if (list.size != 2) {
    throw IllegalStateException("Improperly formatted optional")
  }
  return textMap[list[0]] == list[1]
}

fun loadImageFromFilename(cache: FileImageCache, resources: GameResources, filename: String?): ImmutableImage? {
  if (filename == null) {
    return null
  }
  if (cache.hasImage(filename)) {
    return cache.getImage(filename)
  }
  val f = findFileFromImageText(resources, filename) ?: return null
  val image = ImmutableImage.loader().fromFile(f)
  cache.addImage(filename, image)
  return image
}

/**
 * Finds a file using the argument filename. First this tries to find the filename itself, and if nothing comes up, then
 * it treats the filename argument as a resource name, and then gets the related string from the GameResources to use
 * for looking for a while.
 */
fun findFileFromImageText(resources: GameResources, filename: String?): File? {
  if (filename == null) {
    return null
  }
  val f = findFile(filename)
  if (f != null) {
    return f
  } else {
    return findFile(resources.getString(filename))
  }
}

fun findFile(filename: String): File? {
  val start = File(".")
  start.walk().forEach {
    if (it.name == filename) {
      return it
    }
  }
  return null
}

fun resolveIntField(resources: GameResources, str: String?): Int? {
  if (str == null) {
    return null
  }

  if (str.isBlank()) {
    throw IllegalArgumentException("Invalid int field")
  }

  val intVal = str.toIntOrNull()
  if (intVal != null) {
    return intVal
  }

  for (property in SpecialIntProperty.values()) {
    val propNameLowerCase = property.name.toLowerCase()
    if (str.matches(Regex.fromLiteral("\$$propNameLowerCase"))) {
      return property.prop
    }
  }

  val newStr = replaceIds(resources, str)
  if (newStr.toIntOrNull() != null) {
    return newStr.toInt()
  }

  return Expressions().setRoundingMode(RoundingMode.HALF_UP).eval(newStr).intValueExact()
}

fun resolveDoubleField(resources: GameResources, str: String?): Double? {
  if (str == null) {
    return null
  }

  if (str.isBlank()) {
    throw IllegalArgumentException("Invalid double field")
  }

  val doubleVal = str.toDoubleOrNull()
  if (doubleVal != null) {
    return doubleVal
  }

  return resources.getDouble(str)
}

fun resolveStringField(resources: GameResources, str: String?): String? {
  if (str == null) {
    return null
  }

  if (str.isBlank()) {
    throw IllegalArgumentException("Invalid string field")
  }

  if (resources.containsString(str)) {
    return resources.getString(str)
  }

  return str
}

fun countChars(str: String, ch: Char): Int {
  return str.filter { it == ch }.count()
}

fun resolveVerticalAlignment(str: String?): VerticalAlignment {
  if (str == null) {
    return VerticalAlignment(VerticalAlignmentProperty.PARENT_TOP)
  } else if (str.isBlank()){
    throw IllegalArgumentException("Invalid alignment value: must be non-blank")
  }

  if (str.contains("@")) {
    val pair = splitAlignmentValue(str)
    when (pair.first) {
      AlignmentType.ALIGNED_TO_ELEMENT -> {
        return VerticalAlignment(VerticalAlignmentProperty.valueOf(pair.second[0].toUpperCase()),
        VerticalAlignmentProperty.valueOf(pair.second[1].toUpperCase()), pair.second[2], null)}
      AlignmentType.ALIGNED_BETWEEN_ELEMENTS -> {
        return VerticalAlignment(
          VerticalAlignmentProperty.valueOf(pair.second[0].toUpperCase()), null, pair.second[1], pair.second[2])
      }
      else -> throw IllegalArgumentException("Invalid alignment value: unrecognized format")
    }
  } else {
    val newStr = str.removePrefix("$")
    return VerticalAlignment(VerticalAlignmentProperty.valueOf(newStr.toUpperCase()))
  }
}

fun resolveHorizontalAlignment(str: String?): HorizontalAlignment {
  if (str == null) {
    return HorizontalAlignment(HorizontalAlignmentProperty.PARENT_LEFT)
  } else if (str.isBlank()){
    throw IllegalArgumentException("Invalid alignment value: must be non-blank")
  }

  if (str.contains("@")) {
    val pair = splitAlignmentValue(str)
    when (pair.first) {
      AlignmentType.ALIGNED_TO_ELEMENT -> {
        return HorizontalAlignment(HorizontalAlignmentProperty.valueOf(pair.second[0].toUpperCase()),
          HorizontalAlignmentProperty.valueOf(pair.second[1].toUpperCase()), pair.second[2], null)}
      AlignmentType.ALIGNED_BETWEEN_ELEMENTS -> {
        return HorizontalAlignment(
          HorizontalAlignmentProperty.valueOf(pair.second[0].toUpperCase()), null, pair.second[1], pair.second[2])
      }
      else -> throw IllegalArgumentException("Invalid alignment value: unrecognized format")
    }
  } else {
    val newStr = str.removePrefix("$")
    return HorizontalAlignment(HorizontalAlignmentProperty.valueOf(newStr.toUpperCase()))
  }
}

fun splitAlignmentValue(str: String): Pair<AlignmentType, List<String>> {
  val list = str.split('@').toMutableList()
  if (countChars(str, '$') == 2 && countChars(str, '@') == 1) {
    val secondList = list[0].removePrefix("$").split("$")
    return Pair(AlignmentType.ALIGNED_TO_ELEMENT, listOf(secondList[0], secondList[1], list[1]))
  } else if (countChars(str, '$') == 1 && countChars(str, '@') == 2) {
    return Pair(AlignmentType.ALIGNED_BETWEEN_ELEMENTS, listOf(list[0].removePrefix("$"), list[1], list[2]))
  } else {
    throw IllegalArgumentException("Invalid alignment value: unrecognized format")
  }
}

fun replaceIds(resources: GameResources, str: String): String {
  var newStr = str
  val idRegex = Regex("[a-z][a-z_]*")

  var matchResult = idRegex.find(newStr)
  while (matchResult != null) {
    for (subStr in matchResult.groupValues) {
      if (!resources.containsInt(subStr)) {
        throw IllegalArgumentException("Unrecognized identifier: $subStr")
      }
      newStr = newStr.replace(subStr, resources.getInt(subStr).toString())
    }
    matchResult = idRegex.find(newStr)
  }

  return newStr
}
