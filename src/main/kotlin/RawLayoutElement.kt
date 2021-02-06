package com.cicero.cardgame

/**
 * This class is used for parsing direct from the json.
 */
data class RawLayoutElement(
  val id: String,
  val width: String? = null,
  val height: String? = null,
  val align_vertical: String? = null,
  val align_top_edge: String? = null,
  val align_bottom_edge: String? = null,
  val align_horizontal: String? = null,
  val align_left_edge: String? = null,
  val align_right_edge: String? = null,
  val horizontal_margin: String? = null,
  val left_margin: String? = null,
  val right_margin: String? = null,
  val vertical_margin: String? = null,
  val top_margin: String? = null,
  val bottom_margin: String? = null,
  val horizontal_padding: String? = null,
  val left_padding: String? = null,
  val right_padding: String? = null,
  val vertical_padding: String? = null,
  val top_padding: String? = null,
  val bottom_padding: String? = null,
  val image: String? = null,
  val image_list: String? = null,
  val image_count: String? = null,
  val flip_vertical: String? = null,
  val optional: String? = null,
  val text: String? = null,
  val font_name: String? = null,
  val font_size: String? = null,
  val max_text_row_count: String? = null,
  val children: List<RawLayoutElement>? = null,
)
