package com.cicero.cardgame.resources

import com.cicero.cardgame.GameDefinition
import java.io.File
import kotlin.math.roundToInt

fun readResourcesFile(filename: String): GameResources {
  val stringMap = mutableMapOf<String, String>()
  val intMap = mutableMapOf<String, Int>()
  val doubleMap = mutableMapOf<String, Double>()

  val lines = File(filename).bufferedReader().readLines()
  for (line in lines) {
    if (line.isBlank() || line.get(0) == '#') {
      continue
    }

    val sections = line.split("=")
    if (sections.size == 2) {
      val propertyName = sections[0].trim()
      val value = sections[1].trim()

      val asInt = value.toIntOrNull()
      val asDouble = value.toDoubleOrNull()
      if (asInt != null && asDouble != null) {
        if (asInt == asDouble.roundToInt()) {
          intMap[propertyName] = asInt
        } else {
          doubleMap[propertyName] = asDouble
        }
      } else if (asInt != null) {
        intMap[propertyName] = asInt
      } else if (asDouble != null) {
        doubleMap[propertyName] = asDouble
      } else {
        stringMap[propertyName] = value.trimStart('\"').trimEnd('\"')
      }
    } else {
      throw IllegalStateException("Lines that are not comments or whitespace must have exactly one equals sign")
    }
  }

  println(stringMap)

  return GameResources(stringMap, intMap, doubleMap)
}

fun createResourcesFromGameDefinition(gameDef: GameDefinition): GameResources {
  val stringMap = mutableMapOf<String, String>()
  val intMap = mutableMapOf<String, Int>()
  val doubleMap = mutableMapOf<String, Double>()

  for (key in gameDef.game_resources.keys) {
    val value = gameDef.game_resources[key] ?: throw IllegalStateException("Couldn't find value for $key")
    val asInt = value.toIntOrNull()
    val asDouble = value.toDoubleOrNull()
    if (asInt != null && asDouble != null) {
      if (asInt == asDouble.roundToInt()) {
        intMap[key] = asInt
      } else {
        doubleMap[key] = asDouble
      }
    } else if (asInt != null) {
      intMap[key] = asInt
    } else if (asDouble != null) {
      doubleMap[key] = asDouble
    } else {
      stringMap[key] = value.trimStart('\"').trimEnd('\"')
    }
  }

  println(stringMap)

  return GameResources(stringMap, intMap, doubleMap)
}