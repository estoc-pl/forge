package com.github.andrewkuryan.forge.automata

sealed interface BaseNSASignal

sealed class NSASignal {
    data class Symbol(val value: Char) : NSASignal(), BaseNSASignal, InputSignal, StackSignal {
        override fun toString() = value.toString()
    }

    data class Range(val value: CharRange) : NSASignal(), BaseNSASignal, InputSignal, StackSignal {
        override fun toString() = "${value.first}-${value.last}"
    }

    data class Not(
        val first: BaseNSASignal,
        val rest: List<BaseNSASignal> = listOf(),
    ) : NSASignal(), InputSignal, StackSignal {
        override fun toString() = (listOf(first) + rest).joinToString(
            "",
            if (rest.isEmpty()) "^" else "[^",
            if (rest.isEmpty()) "" else "]"
        )
    }
}