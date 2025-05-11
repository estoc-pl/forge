package com.github.andrewkuryan.forge.automata

sealed interface BaseNSASignal

sealed class NSASignal : InputSignal, StackSignal {
    data class Symbol(val value: Char) : NSASignal(), BaseNSASignal {
        override fun toString() = value.toString()
    }

    data class Range(val value: CharRange) : NSASignal(), BaseNSASignal {
        override fun toString() = "${value.first}-${value.last}"
    }

    data class Not(val first: BaseNSASignal, val rest: List<BaseNSASignal> = listOf()) : NSASignal() {
        override fun toString() = (listOf(first) + rest).joinToString(
            "",
            if (rest.isEmpty()) "^" else "[^",
            if (rest.isEmpty()) "" else "]"
        )
    }
}