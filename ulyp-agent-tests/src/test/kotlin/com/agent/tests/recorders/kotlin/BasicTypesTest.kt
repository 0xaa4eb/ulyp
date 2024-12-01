package com.agent.tests.recorders.kotlin

fun getPair(): Pair<String, Int> {
    return "ABC" to 42
}

fun getPair2(): Pair<String, Int?> {
    return "ABC" to null
}
