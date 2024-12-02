package com.agent.tests.recorders.kotlin

fun getPair(): Pair<String, Int> {
    return "ABC" to 42
}

fun getPair2(): Pair<String, Int?> {
    return "ABC" to null
}

fun getTriple(): Triple<String, Int, String> {
    return Triple("ABC", 42, "ZXVZC")
}

fun getTriple2(): Triple<String, Int, String?> {
    return Triple("ABC", 42, null)
}
