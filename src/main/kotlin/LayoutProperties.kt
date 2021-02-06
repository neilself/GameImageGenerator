package com.cicero.cardgame

/**
 * This file contains a grab bag of simple enum and data classes using for assisting with laying things out.
 */

enum class SpecialIntProperty(val prop: Int) {
  USE_IMAGE_WIDTH(-1),
  USE_IMAGE_HEIGHT(-2),
  MATCH_PARENT(-3),
}

enum class VerticalAlignmentProperty {
  PARENT_TOP,
  PARENT_BOTTOM,
  PARENT_CENTER_VERTICAL,
  TOP_EDGE_TO,
  BOTTOM_EDGE_TO,
  TOP_EDGE_OF,
  BOTTOM_EDGE_OF,
  CENTERED_BETWEEN,
}

enum class HorizontalAlignmentProperty {
  PARENT_LEFT,
  PARENT_RIGHT,
  PARENT_CENTER_HORIZONTAL,
  LEFT_EDGE_TO,
  RIGHT_EDGE_TO,
  RIGHT_EDGE_OF,
  LEFT_EDGE_OF,
  CENTERED_BETWEEN,
}

enum class AlignmentType {
  SIMPLE_PROPERTY,
  ALIGNED_TO_ELEMENT,
  ALIGNED_BETWEEN_ELEMENTS,
}

data class VerticalAlignment(val prop1: VerticalAlignmentProperty, val prop2: VerticalAlignmentProperty? = null,
                             val targetId1: String? = null, val targetId2: String? = null) {

  init {
    assert(isSimplePropertyMode() xor isAlignedToElementMode())
      { "VerticalAlignment can only be in one possible mode" }
  }

  fun isSimplePropertyMode(): Boolean {
    return (prop1 == VerticalAlignmentProperty.PARENT_TOP
            || prop1 == VerticalAlignmentProperty.PARENT_BOTTOM
            || prop1 == VerticalAlignmentProperty.PARENT_CENTER_VERTICAL)
            && prop2 == null && targetId1 == null && targetId2 == null
  }

  fun isAlignedToElementMode(): Boolean {
    return (prop1 == VerticalAlignmentProperty.TOP_EDGE_TO || prop1 == VerticalAlignmentProperty.BOTTOM_EDGE_TO)
            && (prop2 == VerticalAlignmentProperty.TOP_EDGE_OF || prop2 == VerticalAlignmentProperty.BOTTOM_EDGE_OF)
            && targetId1 != null && targetId2 == null
  }

  fun isAlignedBetweenElementsMode(): Boolean {
    return prop1 == VerticalAlignmentProperty.CENTERED_BETWEEN && prop2 == null
            && targetId1 != null && targetId2 != null
  }
}

data class HorizontalAlignment(val prop1: HorizontalAlignmentProperty, val prop2: HorizontalAlignmentProperty? = null,
                               val targetId1: String? = null, val targetId2: String? = null) {

  init {
    assert(isSimplePropertyMode() xor isAlignedToElementMode())
      { "HorizontalAlignment can only be in one possible mode" }
  }

  fun isSimplePropertyMode(): Boolean {
    return (prop1 == HorizontalAlignmentProperty.PARENT_LEFT
            || prop1 == HorizontalAlignmentProperty.PARENT_RIGHT
            || prop1 == HorizontalAlignmentProperty.PARENT_CENTER_HORIZONTAL)
            && prop2 == null && targetId1 == null && targetId2 == null
  }

  fun isAlignedToElementMode(): Boolean {
    return (prop1 == HorizontalAlignmentProperty.LEFT_EDGE_TO || prop1 == HorizontalAlignmentProperty.RIGHT_EDGE_TO)
            && (prop2 == HorizontalAlignmentProperty.LEFT_EDGE_OF || prop2 == HorizontalAlignmentProperty.RIGHT_EDGE_OF)
            && targetId1 != null && targetId2 == null
  }

  fun isAlignedBetweenElementsMode(): Boolean {
    return prop1 == HorizontalAlignmentProperty.CENTERED_BETWEEN && prop2 == null
            && targetId1 != null && targetId2 != null
  }
}