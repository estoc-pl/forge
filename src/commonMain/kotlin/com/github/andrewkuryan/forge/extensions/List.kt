package com.github.andrewkuryan.forge.extensions

fun <T> List<T>.endsWith(other: List<T>): Boolean =
    when (other.size) {
        0 -> true
        else -> when (size) {
            0 -> false
            else -> if (last() == other.last()) dropLast(1).endsWith(other.dropLast(1)) else false
        }
    }