package com.cicero.cardgame

import com.beust.klaxon.Klaxon
import com.cicero.cardgame.resources.GameResources
import com.cicero.cardgame.resources.MainResources.Companion.CARD_SHEET_ROW_SIZE
import com.sksamuel.scrimage.ImmutableImage
import java.awt.*
import java.awt.font.FontRenderContext
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import java.text.AttributedString

class ImageGenerator(private val resources: GameResources, private val drawBoundingBoxes: Boolean = false) {

  // TODO: Maybe should just throw an exception if these aren't specified on an element with text?
  private val defaultTextColor = Color(33, 33, 33)
  private val defaultFontFamily = "Bookman Old Style"
  private val defaultFontSize = 36

  fun createImageSheet(imageList: List<ImmutableImage>): ImmutableImage {
    if (imageList.isEmpty()) {
      return ImmutableImage.create(0, 0)
    }

    val imageWidth = imageList[0].width
    val imageHeight = imageList[0].height
    val rowSize = resources.getInt(CARD_SHEET_ROW_SIZE.toLowerCase())
    val sheetWidth = imageWidth * rowSize
    val sheetHeight = imageHeight * ((imageList.size / rowSize) + (if (imageList.size % rowSize == 0) 0 else 1))
    var sheetImage = ImmutableImage.create(sheetWidth, sheetHeight)

    for (i in imageList.indices) {
      val x = (i % rowSize) * imageWidth
      val y = (i / rowSize) * imageHeight
      sheetImage = sheetImage.overlay(imageList[i], x, y)
    }

    return sheetImage
  }

  fun createCards(dataTable: DataTable, filenameId: String): List<ImmutableImage> {
    val rawLayout = getRawLayout(filenameId)
    val imagesList = mutableListOf<ImmutableImage>()
    for (rowId in dataTable.getRowIdSet()) {
      val layout = createFromRawLayoutElement(resources, dataTable.getMapForRowId(rowId), rawLayout)
      imagesList.add(createImageFromLayout(layout, mutableMapOf(), Point(0, 0)))
    }
    return imagesList
  }

  private fun getRawLayout(filenameId: String): RawLayoutElement {
    val text = File(resources.getString(filenameId)).bufferedReader().readText()
    return Klaxon()
      .parse<RawLayoutElement>(text) ?: throw IllegalStateException("Unable to parse raw layout element")
  }

  /**
   * Where the magic happens: the last phase of laying things out. In this recursive function, LayoutElements are given
   * their final positions and sizes, based on their various properties. Text is also rendered here, though in hindsight
   * maybe it shouldn't be?
   */
  private fun createImageFromLayout(element: LayoutElement, idBoxMap: MutableMap<String,
          Rectangle>, offset: Point):
          ImmutableImage {
    var overallImage = ImmutableImage.create(element.width, element.height)

    // Determine content size
    var contentImage = element.image
    var contentWidth = element.width - (element.leftPadding + element.rightPadding)
    var contentHeight = element.height - (element.topPadding + element.bottomPadding)
    if (contentImage != null && (contentImage.width != contentWidth || contentImage.height != contentHeight)) {
      contentImage = contentImage.scaleTo(contentWidth, contentHeight)
    }
    val offsetWithPadding = Point(offset.x + element.leftPadding, offset.y + element.topPadding)

    // Overlay the content image onto the overall image, if one is present
    if (contentImage != null) {
      overallImage = overallImage.overlay(contentImage, element.leftPadding, element.topPadding)
    }

    // Overlay a text image onto the overall image, if one is present
    if (element.text != null) {
      // TODO: Have to add something for font style
      val font = Font(
        element.fontName ?: defaultFontFamily,
        Font.PLAIN,
        if (element.fontSize != 0) element.fontSize else defaultFontSize
      )
      val textImage = createTextImage(element.text, font, contentWidth, contentHeight, element.maxTextRowCount)
      overallImage = overallImage.overlay(textImage, element.leftPadding, element.topPadding)
    }

    // Overlay child images onto the overall image, if children are present
    if (element.children != null) {
      for (child in element.children) {

        // Determine x and y positions for the child, if those are simple properties
        var childPositionX = 0
        if (child.alignHorizontal.isSimplePropertyMode()) {
          childPositionX = when (child.alignHorizontal.prop1) {
            HorizontalAlignmentProperty.PARENT_CENTER_HORIZONTAL -> (contentWidth - child.width) / 2
            HorizontalAlignmentProperty.PARENT_RIGHT -> contentWidth - child.rightMargin - child.width +
                    element.leftPadding

            // These two should be the same
            HorizontalAlignmentProperty.PARENT_LEFT -> child.leftMargin + element.leftPadding
            else -> child.leftMargin
          }
        }
        var childPositionY = 0
        if (child.alignVertical.isSimplePropertyMode()) {
          childPositionY = when (child.alignVertical.prop1) {
            VerticalAlignmentProperty.PARENT_CENTER_VERTICAL -> (contentHeight - child.height) / 2
            VerticalAlignmentProperty.PARENT_BOTTOM -> contentHeight - child.bottomMargin - child.height + element.topPadding

            // These two should be the same
            VerticalAlignmentProperty.PARENT_TOP -> child.topMargin + element.topPadding
            else -> child.topMargin
          }
        }

        // Recurse!
        var childImage = createImageFromLayout(child, idBoxMap, Point(offsetWithPadding.x + childPositionX,
          offsetWithPadding.y + childPositionY))

        // These types of alignment happen after the image is created, rather than before, because they need the
        // image to already be created to use its size for laying out.
        if (child.alignHorizontal.isAlignedToElementMode()) {
          childPositionX = resolveSingleTargetHorizontalEdgeAlignment(child, childImage, offsetWithPadding, idBoxMap)
        } else if (child.alignHorizontal.isAlignedBetweenElementsMode()) {
          childPositionX = resolveBetweenTargetsHorizontalAlignment(child, childImage, offsetWithPadding, idBoxMap)
        }
        if (child.alignVertical.isAlignedToElementMode()) {
          childPositionY = resolveSingleTargetVerticalEdgeAlignment(child, childImage, offsetWithPadding, idBoxMap)
        } else if (child.alignVertical.isAlignedBetweenElementsMode()) {
          childPositionY = resolveBetweenTargetsVerticalAlignment(child, childImage, offsetWithPadding, idBoxMap)
        }

        // For debugging
        LayoutUtils.drawGraphicsBox(childImage, drawBoundingBoxes)

        // Overlay the new child image on top of the overall one
        overallImage = overallImage.overlay(childImage, childPositionX, childPositionY)

        // Record the child's bounding rectangle in the box map
        idBoxMap[child.id] = Rectangle(
          offsetWithPadding.x + childPositionX, offsetWithPadding.y + childPositionY, childImage.width, childImage.height)
      }
    }

    return overallImage
  }

  private fun resolveSingleTargetVerticalEdgeAlignment(child: LayoutElement, childImage: ImmutableImage, offset: Point, idBoxMap:
  Map<String, Rectangle>): Int {
    val childEdge = child.alignVertical.prop1
    val targetEdge = child.alignVertical.prop2 ?:
      throw IllegalArgumentException("Called resolveVerticalEdgeAlignment() without target to resolve")
    val targetBox = idBoxMap[child.alignVertical.targetId1] ?:
      throw IllegalArgumentException("Target box not found in boxMap for ${child.alignVertical.targetId1}")
    val topOfTarget = targetBox.y
    val bottomOfTarget = targetBox.y + targetBox.height

    return if (childEdge == VerticalAlignmentProperty.BOTTOM_EDGE_TO) {
      if (targetEdge == VerticalAlignmentProperty.BOTTOM_EDGE_OF) {
        bottomOfTarget
      } else {
        topOfTarget
      } - childImage.height - child.bottomMargin
    } else {
      if (targetEdge == VerticalAlignmentProperty.BOTTOM_EDGE_OF) {
        bottomOfTarget
      } else {
        topOfTarget
      } + child.topMargin
    } - offset.y
  }

  private fun resolveBetweenTargetsVerticalAlignment(child: LayoutElement, childImage: ImmutableImage, offset: Point,
                                                     idBoxMap: Map<String, Rectangle>): Int {
    val targetBox1 = idBoxMap[child.alignVertical.targetId1] ?: throw IllegalArgumentException("Called " +
            "resolveBetweenTargetsVerticalAlignment() without target1 box available")
    val targetBox2 = idBoxMap[child.alignVertical.targetId2] ?: throw IllegalArgumentException("Called " +
            "resolveBetweenTargetsVerticalAlignment() without target2 box available")

    // TODO: Might need to factor in vertical margins here
    if (targetBox1.y + targetBox1.height < targetBox2.y) {
      // Bottom of first box is above top of second box
      val centerPointBetween = targetBox1.y + targetBox1.height + ((targetBox2.y - (targetBox1.y + targetBox1.height)) / 2)
      return centerPointBetween - (childImage.height / 2)
    } else {
      // Otherwise, assume bottom of second box is above top of first box
      val centerPointBetween = targetBox2.y + targetBox2.height + ((targetBox1.y - (targetBox2.y + targetBox2.height)) / 2)
      return centerPointBetween - (childImage.height / 2)
    }
  }

  private fun resolveSingleTargetHorizontalEdgeAlignment(child: LayoutElement, childImage: ImmutableImage, offset: Point, idBoxMap:
  Map<String, Rectangle>): Int {
    val childEdge = child.alignHorizontal.prop1
    val targetEdge = child.alignHorizontal.prop2 ?:
      throw IllegalArgumentException("Called resolveHorizontalEdgeAlignment() without target to resolve")
    val targetBox = idBoxMap[child.alignHorizontal.targetId1] ?:
      throw IllegalArgumentException("Target box not found in boxMap for ${child.alignHorizontal.targetId1}")
    val leftEdgeOfTarget = targetBox.x
    val rightEdgeOfTarget = targetBox.x + targetBox.width

    return if (childEdge == HorizontalAlignmentProperty.LEFT_EDGE_TO) {
      if (targetEdge == HorizontalAlignmentProperty.RIGHT_EDGE_OF) {
        rightEdgeOfTarget
      } else {
        leftEdgeOfTarget
      } + child.leftMargin
    } else {
      if (targetEdge == HorizontalAlignmentProperty.RIGHT_EDGE_OF) {
        rightEdgeOfTarget
      } else {
        leftEdgeOfTarget
      } - childImage.width - child.rightMargin
    } - offset.x
  }

  private fun resolveBetweenTargetsHorizontalAlignment(child: LayoutElement, childImage: ImmutableImage, offset: Point,
                                                     idBoxMap: Map<String, Rectangle>): Int {
    val targetBox1 = idBoxMap[child.alignHorizontal.targetId1] ?: throw IllegalArgumentException("Called " +
            "resolveBetweenTargetsHorizontalAlignment() without target1 box available")
    val targetBox2 = idBoxMap[child.alignHorizontal.targetId2] ?: throw IllegalArgumentException("Called " +
            "resolveBetweenTargetsHorizontalAlignment() without target2 box available")

    // TODO: Might need to factor in vertical margins here
    if (targetBox1.x + targetBox1.width < targetBox2.x) {
      // Right edge of first box is to the left of left edge of second box
      val centerPointBetween =
        targetBox1.x + targetBox1.width + ((targetBox2.x - (targetBox1.x + targetBox1.width)) / 2)
      return centerPointBetween - (childImage.width / 2)
    } else {
      // Otherwise, assume right edge of second box is to the left of left edge of first box
      val centerPointBetween =
        targetBox2.x + targetBox2.width + ((targetBox1.x - (targetBox2.x + targetBox2.width)) / 2)
      return centerPointBetween - (childImage.width / 2)
    }
  }

  private fun createTextImage(text: String, font: Font, widthBounds: Int, heightBounds: Int, maxRowCount: Int):
          ImmutableImage {
    val buffImage = BufferedImage(widthBounds, heightBounds, TYPE_INT_ARGB)
    val graphics = buffImage.createGraphics()
    graphics.color = defaultTextColor

    LayoutUtils.drawGraphicsBox(graphics, widthBounds, heightBounds, drawBoundingBoxes)

    val attributedDescription = AttributedString(text)
    attributedDescription.addAttribute(TextAttribute.FONT, font)
    val lineMeasurer = LineBreakMeasurer(attributedDescription.iterator, FontRenderContext(graphics.fontRenderContext.transform, true, graphics
      .fontRenderContext.usesFractionalMetrics()))
    val paragraphStart = attributedDescription.iterator.beginIndex
    val paragraphEnd = attributedDescription.iterator.endIndex

    val actualTextHeight = computeTotalTextHeight(lineMeasurer, paragraphStart, paragraphEnd, widthBounds,
      maxRowCount)
    var drawPosY = (heightBounds / 2) - (actualTextHeight / 2)
    lineMeasurer.position = paragraphStart

    while (lineMeasurer.position < paragraphEnd) {
      val layout = lineMeasurer.nextLayout(widthBounds.toFloat())
      val drawPosX = 0f
      drawPosY += layout.ascent

      layout.draw(graphics, drawPosX + ((widthBounds - layout.bounds.width) / 2).toFloat(), drawPosY)

      drawPosY += layout.descent + layout.leading
    }

    return ImmutableImage.fromAwt(buffImage)
  }

  private fun computeTotalTextHeight(lineMeasurer: LineBreakMeasurer, paragraphStart: Int, paragraphEnd: Int,
                                     textWidthBounds: Int,
                                     maxTextRowCount: Int):
          Float {
    var drawPosY = 0f
    var rowCount = 0
    var lastLeading = 0f
    lineMeasurer.position = paragraphStart
    while (lineMeasurer.position < paragraphEnd) {
      val layout = lineMeasurer.nextLayout(textWidthBounds.toFloat())
      drawPosY += layout.ascent + layout.descent + layout.leading
      rowCount += 1

      lastLeading = layout.leading
    }

    if (rowCount > maxTextRowCount) {
      throw IllegalStateException("Number of text rows may not exceed maximum of $maxTextRowCount")
    }

    // "Leading" is the margin between lines of text, so for the purposes of centering we should leave it out when
    // calculating total height
    return drawPosY - lastLeading
  }
}
