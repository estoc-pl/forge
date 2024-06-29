package com.github.andrewkuryan.forge.automata

sealed class StackAction {
    object None : StackAction() {
        override fun toString() = "-"
    }
}

data class Push(val letter: StackLetter) : StackAction() {
    override fun toString() = letter.toString()
}

data class Rollup(val target: StackLetter, val top: List<StackFrame>) : StackAction() {
    override fun toString() = "${top.joinToString("")} → $target"
}