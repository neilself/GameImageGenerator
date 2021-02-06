package com.cicero.cardgame.resources

open class GameResources() {

  protected val stringMap = mutableMapOf<String, String>()
  protected val intMap = mutableMapOf<String, Int>()
  protected val doubleMap = mutableMapOf<String, Double>()

  constructor(sMap: Map<String, String>, iMap: Map<String, Int>, dMap: Map<String, Double>) : this() {
    stringMap.putAll(sMap)
    intMap.putAll(iMap)
    doubleMap.putAll(dMap)
  }

  fun addResources(res: GameResources) {
    stringMap.putAll(res.stringMap)
    intMap.putAll(res.intMap)
    doubleMap.putAll(res.doubleMap)
  }

  fun getString(key: String): String {
    return stringMap[key] ?: throw IllegalArgumentException("Couldn't find $key in string mapping")
  }

  fun getInt(key: String): Int {
    return intMap[key] ?: throw IllegalArgumentException("Couldn't find $key in int mapping")
  }

  fun getDouble(key: String): Double {
    return doubleMap[key] ?: throw IllegalArgumentException("Couldn't find $key in double mapping")
  }

  fun containsString(key: String): Boolean {
    return stringMap.containsKey(key)
  }

  fun containsInt(key: String): Boolean {
    return intMap.containsKey(key)
  }

  fun containsDouble(key: String): Boolean {
    return doubleMap.containsKey(key)
  }
}