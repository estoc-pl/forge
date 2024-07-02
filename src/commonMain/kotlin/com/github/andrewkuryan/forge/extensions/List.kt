package com.github.andrewkuryan.forge.extensions

import kotlin.math.min

fun <T> List<T>.endsWith(other: List<T>): Boolean =
    if (size < other.size) false
    else subList(size - other.size, size) == other

fun <T> commonSuffix(list1: List<T>, list2: List<T>): List<T> =
    (0 until min(list1.size, list2.size))
        .indexOfFirst { list1[list1.size - 1 - it] != list2[list2.size - 1 - it] }
        .let {
            if (it == -1) minOf(list1, list2) { l1, l2 -> l1.size.compareTo(l2.size) }
            else list1.subList(list1.size - it, list1.size)
        }

fun <T> commonPrefix(list1: List<T>, list2: List<T>): List<T> =
    (0 until min(list1.size, list2.size))
        .indexOfFirst { list1[it] != list2[it] }
        .let {
            if (it == -1) minOf(list1, list2) { l1, l2 -> l1.size.compareTo(l2.size) }
            else list1.subList(0, it)
        }