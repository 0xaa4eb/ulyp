package com.agent.tests.recorders.kotlin

fun getImmutableMap(): Map<String, String> {
    return mapOf("1" to "ABC", "2" to "CDE", "3" to "EFG")
}

fun getMutableMap(): MutableMap<String, String> {
    return mutableMapOf("1" to "ABC", "2" to "CDE", "3" to "EFG")
}

fun getImmutableList(): List<String> {
    return listOf("ABC", "CDE", "EFG", "FGH", "HJK")
}

fun getEmptyList(): List<String> {
    return emptyList()
}

fun getArrayDequeue(): ArrayDeque<String> {
    val deq = ArrayDeque<String>(1)
    deq.add("A")
    deq.add("B")
    deq.add("C")
    deq.add("E")
    deq.add("F")
    return deq
}

fun getMutableList(): MutableList<String> {
    return mutableListOf("ABC", "CDE", "EFG", "FGH", "HJK")
}

fun getArray(): IntArray {
    return intArrayOf(1, 2, 3, 4, 5)
}