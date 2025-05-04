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

 fun parseStackSignals(rawSignals: String): List<StackSignal> {
    val bottom = if (rawSignals.endsWith("$")) listOf(StackSignal.Bottom) else listOf()
    return listOf(
        IntRange(0, -1),
        *Regex("([A-Z_]+[0-9]*)").findAll(rawSignals).map { it.range }.toList().toTypedArray(),
        IntRange(rawSignals.length - bottom.size, rawSignals.length - 1)
    ).zipWithNext()
        .map { (start, end) ->
            (if (!start.isEmpty()) listOf(StackSignal.Letter(rawSignals.substring(start))) else listOf()) +
                    (start.last + 1 until end.first).map { StackSignal.Letter(rawSignals[it].toString()) }
        }
        .flatten() + bottom
}