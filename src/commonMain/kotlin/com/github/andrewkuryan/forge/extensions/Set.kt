package com.github.andrewkuryan.forge.extensions

fun <T> minOfSize(first: Set<T>, second: Set<T>) = minOf(first, second) { s1, s2 -> s1.size - s2.size }