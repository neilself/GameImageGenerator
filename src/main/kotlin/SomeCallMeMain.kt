package com.cicero.cardgame

import com.cicero.cardgame.resources.MainResources
import com.cicero.cardgame.resources.createResourcesFromGameDefinition
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.sksamuel.scrimage.ImmutableImage
import java.nio.file.Path

fun main() {
//  createCards("pit_wizards.yaml")
//  createCards("fight_game.yaml")
  createCards("gamble_game.yaml")
}

fun createCards(gameDefinitionFilename: String) {
  val startTime = System.currentTimeMillis()

  val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
  val gameDef: GameDefinition = mapper.readValue(Path.of(gameDefinitionFilename).toFile())

  val resources = MainResources()
  resources.addResources(createResourcesFromGameDefinition(gameDef))
  val gen = ImageGenerator(resources, /* drawBoundingBoxes= */ true)
  val totalImageList = mutableListOf<ImmutableImage>()
  val cache = FileImageCache()

  for (map in gameDef.game_entity_collections) {
    val dataTable = LayoutUtils.loadDataTableFromFile(map.data_filename, resources)
    val cardImages = gen.createCards(dataTable, map.layout_filename, cache)
    for (i in cardImages.indices) {
      LayoutUtils.writeImageOut(cardImages[i], "${map.output_filename_prefix}$i", resources)
    }
    totalImageList.addAll(cardImages)
  }

  val timeTakenSec = (System.currentTimeMillis() - startTime.toDouble()) / 1000
  println("Time taken: $timeTakenSec seconds")
}
