package com.cicero.cardgame

/**
 * Classes for parsing YAML game config files.
 */

data class GameEntityCollection(val data_filename: String, val layout_filename: String, val output_filename_prefix:
String)

data class GameDefinition(val game_title: String, val game_entity_collections: List<GameEntityCollection>,
                          val game_resources: Map<String, String>)