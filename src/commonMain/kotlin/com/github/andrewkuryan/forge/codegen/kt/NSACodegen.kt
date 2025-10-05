package com.github.andrewkuryan.forge.codegen.kt

import kotlin.reflect.KClass
import com.github.andrewkuryan.forgeKit.transition.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.automata.format.Formatter
import kotlin.jvm.JvmName

object NSACodegen : Formatter {

    override fun NSA<*>.format(nodeType: KClass<*>) = """val initState = ${initState.format()}
    |val finalStates = setOf(${finalStates.joinToString(",") { it.format() }})
    |val transitions = mapOf<State, Set<MeaningfulTransition<${nodeType.simpleName}>>>(
    |${states.map { it to getOutTransitions(it) }.filter { it.second.isNotEmpty() }.joinToString(",\n") { it.format() }}
    |)""".trimMargin()

    private fun Pair<State, List<MeaningfulTransition<*>>>.format() =
        "\t${first.format()} to setOf${second.joinToString(",\n\t\t", "(\n\t\t", "\n\t)") { it.format() }}"

    override fun State.format() = "State(${index})"

    override fun MeaningfulTransition<*>.format() =
        "MeaningfulTransition(${source.format()},${target.format()},${guard.format()})"

    override fun Guard.Meaningful<*>.format() = when (this) {
        is Guard.Input -> "Guard.Input" + listOf(
            input.formatNamedArg("input"),
            inputPreview.formatNamedArg("inputPreview"),
            stackPreview.formatNamedArg("stackPreview"),
            stackPushBefore.formatNamedArg("stackPushBefore"),
            stackPushAfter.formatNamedArg("stackPushAfter")
        ).filter { it.isNotEmpty() }.joinToString(",", "(", ")")

        is Guard.Stack -> "Guard.Stack" + listOf(
            rollupTarget.formatNamedArg("rollupTarget"),
            stack.formatNamedArg("stack"),
            semanticAction.formatNamedArg("semanticAction"),
            inputPreview.formatNamedArg("inputPreview"),
            stackPreview.formatNamedArg("stackPreview"),
            stackPushBefore.formatNamedArg("stackPushBefore"),
            stackPushAfter.formatNamedArg("stackPushAfter")
        ).filter { it.isNotEmpty() }.joinToString(",", "(", ")")
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatInputSlice")
    override fun InputSlice.format() = "listOf(${joinToString(",") { it.format() }})"

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatStackSlice")
    override fun StackSlice.format() = "listOf(${joinToString(",") { it.format() }})"

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatStackPush")
    override fun StackPush.format() = "listOf(${joinToString(",") { it.format() }})"

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

    @JvmName("formatInputSliceNamedArg")
    private fun InputSlice.formatNamedArg(name: String) = if (isEmpty()) "" else "$name=${format()}"

    @JvmName("formatStackSliceNamedArg")
    private fun StackSlice.formatNamedArg(name: String) = if (isEmpty()) "" else "$name=${format()}"

    @JvmName("formatStackPushNamedArg")
    private fun StackPush.formatNamedArg(name: String) = if (isEmpty()) "" else "$name=${format()}"

    private fun StackSignal.Preview.formatNamedArg(name: String) = "$name=${format()}"
    private fun SemanticAction<*>?.formatNamedArg(name: String) = if (this == null) "" else "$name=${format()}"
}