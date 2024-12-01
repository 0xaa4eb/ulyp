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

fun getMutableList(): MutableList<String> {
    return mutableListOf("ABC", "CDE", "EFG", "FGH", "HJK")
}