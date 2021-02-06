package com.cicero.cardgame

//class GameDefinition {
//  lateinit var game_title: String
//  lateinit var game_card_collections: List<Map<String, String>>
//}

data class GameEntityCollection(val data_filename: String, val layout_filename: String, val output_filename_prefix:
String)

data class GameDefinition(val game_title: String, val game_entity_collections: List<GameEntityCollection>,
                          val game_resources: Map<String, String>)