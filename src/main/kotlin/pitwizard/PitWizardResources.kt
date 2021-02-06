package com.cicero.cardgame.pitwizard

import com.cicero.cardgame.resources.GameResources
import com.cicero.cardgame.resources.readResourcesFile

class PitWizardResources: GameResources() {

  companion object {
    const val INPUT_FILENAME_PRODUCTION_CARD_DATA = "input_filename_production_card_data"
    const val INPUT_FILENAME_EXCHANGE_CARD_DATA = "input_filename_exchange_card_data"
    const val INPUT_FILENAME_UPGRADE_CARD_DATA = "input_filename_upgrade_card_data"
    const val INPUT_FILENAME_UNIT_CARD_DATA = "input_filename_unit_card_data"

    // TODO: Create new directory specifically for layouts and rename constants appropriately
    const val INPUT_FILENAME_UNIT_CARD_LAYOUT = "input_filename_unit_card_layout"
    const val INPUT_FILENAME_PRODUCTION_CARD_LAYOUT = "input_filename_production_card_layout"
    const val INPUT_FILENAME_EXCHANGE_CARD_LAYOUT = "input_filename_exchange_card_layout"
    const val INPUT_FILENAME_UPGRADE_CARD_LAYOUT = "input_filename_upgrade_card_layout"
  }

  private val resourcesFilename = "pitwizards_config.cfg"

  init {
    addResources(readResourcesFile(resourcesFilename))
  }
}