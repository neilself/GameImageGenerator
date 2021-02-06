package com.cicero.cardgame

/**
 * Like the world's simplest DB. Input is a list of lists of strings that came from a CSV.
 */
data class DataTable(private val listOfLists: List<List<String>>)  {

  private val attributes: List<String>
  private val data: Map<String, Map<String, String>>

  init {
    assert(listOfLists.isNotEmpty())
    attributes = listOfLists[0]
    data = mutableMapOf()
    for (i in 1 until listOfLists.size) {
      val row = listOfLists[i]
      val rowMap = mutableMapOf<String, String>()
      for (j in attributes.indices) {
        rowMap[attributes[j]] = row[j]
      }
      data[row[0]] = rowMap
    }
  }

  fun getAttributeList(): List<String> {
    return attributes.toList()
  }

  fun getRowIdSet(): Set<String> {
    return data.keys.toSet()
  }

  fun getAttributeForRowId(rowId: String, attribute: String): String {
    val innerMap = data[rowId] ?: throw IllegalArgumentException("Couldn't find data matching $rowId in table")
    return innerMap[attribute] ?: throw IllegalArgumentException("Couldn't find data matching $attribute for $rowId " +
            "in table")
  }

  fun getMapForRowId(rowId: String): Map<String, String> {
    return data[rowId] ?: throw IllegalArgumentException("Couldn't find data matching $rowId in table")
  }
}