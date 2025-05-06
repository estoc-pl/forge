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
        this is InputTransitionBody && transition is InputTransition ->
            input == transition.input && stackPush == transition.stackPush &&
                    inputPreview == transition.inputPreview && stackPreview == transition.stackPreview

        this is StackTransitionBody && transition is StackTransition ->
            stack == transition.stack && stackPush == transition.stackPush &&
                    inputPreview == transition.inputPreview && stackPreview == transition.stackPreview

        else -> false
    }

fun read(input: Char, stackPreview: String) =
    InputTransitionBody(
        InputSlice(listOf(NSASignal.Symbol(input))),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice(listOf(NSASignal.Symbol(input)))
    )

fun rollup(stackPreview: String, stack: String, target: String) =
    StackTransitionBody(
        StackSlice(parseStackSignals(stack)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSignal.Node(target)
    )

fun exit(stackPreview: String) =
    InputTransitionBody(
        InputSlice(listOf(InputSignal.EOI)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice.EMPTY,
    )

fun parseStackSignals(rawSignals: String): List<StackSignal> {
    return listOf(
        IntRange(0, -1),
        *Regex("([A-Z_]+[0-9]*)|[$]").findAll(rawSignals).map { it.range }.toList().toTypedArray(),
        IntRange(rawSignals.length, rawSignals.length - 1)
    ).zipWithNext()
        .map { (start, end) ->
            val node = when {
                start.isEmpty() -> listOf()
                rawSignals.substring(start) == StackSignal.Bottom.toString() -> listOf(StackSignal.Bottom)
                else -> listOf(StackSignal.Node(rawSignals.substring(start)))
            }
            node + (start.last + 1 until end.first).map { NSASignal.Symbol(rawSignals[it]) }
        }
        .flatten()
}