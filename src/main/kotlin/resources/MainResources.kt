package com.cicero.cardgame.resources

class MainResources: GameResources() {

  companion object {
    const val OUTPUT_IMAGES_DIRECTORY = "output_images_directory"
    const val CARD_SHEET_ROW_SIZE = "card_sheet_row_size"
  }

  private val resourcesFilename = "main_config.cfg"
  
  init {
    addResources(readResourcesFile(resourcesFilename))
  }
}