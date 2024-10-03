package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.*

sealed class TransitionBody {
    abstract val inputPreview: InputSlice
    abstract val stackPreview: StackSlice
}

data class InputTransitionBody(
    val input: InputSlice,
    override val inputPreview: InputSlice,
    override val stackPreview: StackSlice,
    val stackPush: StackSlice,
) : TransitionBody()

data class StackTransitionBody(
    val stack: StackSlice,
    override val inputPreview: InputSlice,
    override val stackPreview: StackSlice,
    val stackPush: StackSignal,
) : TransitionBody()

fun TransitionBody.isSameAs(transition: Transition<*>) =
    when {
        this is InputTransitionBody && transition is InputTransition -> input == transition.input && stackPush == transition.stackPush
        this is StackTransitionBody && transition is StackTransition -> stack == transition.stack && stackPush == transition.stackPush
        else -> false
    } && this.inputPreview == transition.inputPreview && this.stackPreview == transition.stackPreview

fun read(input: Char, stackPreview: String) =
    InputTransitionBody(
        InputSlice(listOf(InputSignal.Letter(input))),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice(listOf(StackSignal.Letter(input.toString())))
    )

fun rollup(stackPreview: String, stack: String, target: String) =
    StackTransitionBody(
        StackSlice(parseStackSignals(stack)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSignal.Letter(target)
    )

fun exit(stackPreview: String) =
    InputTransitionBody(
        InputSlice(listOf(InputSignal.EOI)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice.EMPTY,
    )

private fun parseStackSignals(rawSignals: String) =
    rawSignals.map {
        when (it) {
            '$' -> StackSignal.Bottom
            else -> StackSignal.Letter(it.toString())
        }
    }