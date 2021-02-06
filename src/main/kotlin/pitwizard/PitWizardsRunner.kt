package com.cicero.cardgame.pitwizard

import com.cicero.cardgame.ImageGenerator
import com.cicero.cardgame.LayoutUtils
import com.cicero.cardgame.resources.MainResources
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_EXCHANGE_CARD_DATA
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_EXCHANGE_CARD_LAYOUT
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_PRODUCTION_CARD_DATA
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_PRODUCTION_CARD_LAYOUT
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_UNIT_CARD_DATA
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_UNIT_CARD_LAYOUT
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_UPGRADE_CARD_DATA
import com.cicero.cardgame.pitwizard.PitWizardResources.Companion.INPUT_FILENAME_UPGRADE_CARD_LAYOUT

class PitWizardsRunner {

  /**
   * Create the production and exchange cards, and write their images to disk.
   */
  fun createCards() {
    val resources = MainResources()
    resources.addResources(PitWizardResources())
    val gen = ImageGenerator(resources, /* drawBoundingBoxes= */ true)

    val productionDataTable =
      LayoutUtils.loadDataTableFromFile(INPUT_FILENAME_PRODUCTION_CARD_DATA.toString().toLowerCase(),
        resources)
    val productionCardImages =
      gen.createCards(productionDataTable, INPUT_FILENAME_PRODUCTION_CARD_LAYOUT.toString().toLowerCase())
    for (i in productionCardImages.indices) {
      LayoutUtils.writeImageOut(productionCardImages[i], "test_production_card_$i", resources)
    }

    val exchangeDataTable =
      LayoutUtils.loadDataTableFromFile(INPUT_FILENAME_EXCHANGE_CARD_DATA.toString().toLowerCase(), resources)
    val exchangeCardImages =
      gen.createCards(exchangeDataTable, INPUT_FILENAME_EXCHANGE_CARD_LAYOUT.toString().toLowerCase())
    for (i in exchangeCardImages.indices) {
      LayoutUtils.writeImageOut(exchangeCardImages[i], "test_exchange_card_$i", resources)
    }

    val upgradeDataTable =
      LayoutUtils.loadDataTableFromFile(INPUT_FILENAME_UPGRADE_CARD_DATA.toString().toLowerCase(), resources)
    val upgradeCardImages =
      gen.createCards(upgradeDataTable, INPUT_FILENAME_UPGRADE_CARD_LAYOUT.toString().toLowerCase())
    for (i in upgradeCardImages.indices) {
      LayoutUtils.writeImageOut(upgradeCardImages[i], "test_upgrade_card_$i", resources)
    }

    val unitDataTable =
      LayoutUtils.loadDataTableFromFile(INPUT_FILENAME_UNIT_CARD_DATA.toString().toLowerCase(), resources)
    val unitCardImages =
      gen.createCards(unitDataTable, INPUT_FILENAME_UNIT_CARD_LAYOUT.toString().toLowerCase())
    for (i in unitCardImages.indices) {
      LayoutUtils.writeImageOut(unitCardImages[i], "test_unit_card_$i", resources)
    }

    gen.createImageSheet(exchangeCardImages)
  }
}