package com.github.andrewkuryan.forge.automata.format

import kotlin.reflect.KClass
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.grammar.SemanticAction

object KtSourceFormatter : Formatter {

    override fun NSA<*>.format(nodeType: KClass<*>?) = """val initState = ${initState.format()}
    |val finalStates = listOf(${finalStates.joinToString(",") { it.format() }})
    |val transitions = mapOf(
    |${states.map { it to getOutTransitions(it) }.joinToString(",\n") { it.format(nodeType) }}
    |)""".trimMargin()

    private fun Pair<State, List<MeaningfulTransition<*>>>.format(nodeType: KClass<*>?) =
        "\t${first.format()} to setOf${second.joinToString(",\n\t\t", "(\n\t\t", "\n\t)") { it.format(nodeType) }}"

    override fun State.format() = "State(${index})"

    override fun MeaningfulTransition<*>.format(nodeType: KClass<*>?) =
        "MeaningfulTransition${nodeType?.let { "<${it.simpleName}>" } ?: ""}(" +
                "${source.format()}," +
                "${target.format()}," +
                "${guard.format()})"

    override fun Guard.Meaningful<*>.format() = when (this) {
        is Guard.Input -> "Guard.Input(" +
                "${input.format()}," +
                "${inputPreview.format()}," +
                "${stackPreview.format()}," +
                "${stackPushBefore.format()}," +
                "${stackPushAfter.format()})"

        is Guard.Stack -> "Guard.Stack(" +
                "${stack.format()}," +
                "${rollupTarget.format()}," +
                "${semanticAction.format()}," +
                "${inputPreview.format()}," +
                "${stackPreview.format()}," +
                "${stackPushBefore.format()}," +
                "${stackPushAfter.format()})"
    }

    override fun InputSlice.format() =
        if (this.isEmpty) "InputSlice.EMPTY"
        else "InputSlice(listOf(${value.joinToString(",") { it.format() }}))"

    override fun StackSlice.format() =
        if (this.isEmpty) "StackSlice.EMPTY"
        else "StackSlice(listOf(${value.joinToString(",") { it.format() }}))"

    override fun StackPush.format() =
        if (this.isEmpty) "StackPush.EMPTY"
        else "StackPush(listOf(${value.joinToString(",") { it.format() }}))"

    override fun InputSignal.format() = when (this) {
        is InputSignal.Unitary -> format()
        is InputSignal.Not -> "InputSignal.Not(${first.format()},listOf(${rest.joinToString(",") { it.format() }}))"
    }

    private fun InputSignal.Unitary.format() = when (this) {
        is InputSignal.EOI -> "InputSignal.EOI"
        is InputSignal.Symbol -> "InputSignal.Symbol('${value}')"
        is InputSignal.Range -> "InputSignal.Range('${value.first}'..'${value.last}')"
    }

    override fun StackSignal.Preview.format() = when (this) {
        is StackSignal.Bottom -> "StackSignal.Bottom"
        is StackSignal.Symbol -> "StackSignal.Symbol('${value}')"
        is StackSignal.NodeView -> "StackSignal.NodeView(\"${name}\")"
        is StackSignal.Marker -> "StackSignal.Marker(\"${name}\")"
    }

    override fun SemanticAction<*>?.format() = if (this == null) "null" else "SemanticAction(\"${name}\",::${name})"
}