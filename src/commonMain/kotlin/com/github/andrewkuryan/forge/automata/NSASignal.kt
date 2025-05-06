package com.github.andrewkuryan.forge.automata

sealed interface BaseNSASignal

sealed class NSASignal {
    data class Symbol(val value: Char) : NSASignal(), BaseNSASignal, InputSignal, StackSignal {
        override fun toString() = value.toString()
    }

    data class Range(val value: CharRange) : NSASignal(), BaseNSASignal, InputSignal, StackSignal {
        fun toStringNested() = "${value.first}-${value.last}"

        override fun toString() = "[${toStringNested()}]"
    }

    data class Not(val first: BaseNSASignal, val rest: List<BaseNSASignal>) : NSASignal(), InputSignal, StackSignal {
        override fun toString() = (listOf(first) + rest).joinToString("", "[^", "]") {
            when (it) {
                is Symbol -> it.toString()
                is Range -> it.toStringNested()
            }
        }
    }
}