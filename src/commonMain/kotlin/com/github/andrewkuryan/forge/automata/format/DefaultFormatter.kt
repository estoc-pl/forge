package com.github.andrewkuryan.forge.automata.format

import kotlin.reflect.KClass
import com.github.andrewkuryan.forgeKit.transition.*
import com.github.andrewkuryan.forge.automata.*
import kotlin.jvm.JvmName

object DefaultFormatter : Formatter {

    override fun NSA<*>.format(nodeType: KClass<*>) = """NSA(
    |    Q = ${states.joinToString(", ", "{", "}") { it.format() }}
    |    ẟ = ${states.flatMap(::getOutTransitions).joinToString(",\n\t\t", "{\n\t\t", "\n\t}") { it.format() }}
    |    q₀ = ${initState.format()}
    |    F = ${finalStates.joinToString(", ", "{", "}") { it.format() }}
    |)""".trimMargin()

    override fun State.format() = "Q${index}"

    override fun MeaningfulTransition<*>.format() = "${source.format()} -> ${guard.format()} -> ${target.format()}"

    override fun Guard.Meaningful<*>.format() = when (this) {
        is Guard.Input -> "${input.format()}⟨${inputPreview.format()}⟩, ⟨${stackPreview.format()}⟩ / ${combinedPushFormat()}"
        is Guard.Stack -> "⟨${inputPreview.format()}⟩, ${stack.format()}⟨${stackPreview.format()}⟩ / ${combinedPushFormat()}"
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatInputSlice")
    override fun InputSlice.format() = if (isEmpty()) "ε" else joinToString("") { it.format() }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatStackSlice")
    override fun StackSlice.format() = if (isEmpty()) "ε" else joinToString("") { it.format() }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatStackPush")
    override fun StackPush.format() = if (isEmpty()) "ε" else joinToString("") { it.format() }

    override fun InputSignal.format() = when (this) {
        is InputSignal.Unitary -> format()
        is InputSignal.Not -> (listOf(first) + rest).joinToString(
            separator = "",
            prefix = if (rest.isEmpty()) "^" else "[^",
            postfix = if (rest.isEmpty()) "" else "]"
        ) { it.format() }
    }

    private fun InputSignal.Unitary.format() = when (this) {
        is InputSignal.EOI -> "┴"
        is InputSignal.Symbol -> value.toString()
        is InputSignal.Range -> "${value.first}-${value.last}"
    }

    override fun StackSignal.Preview.format() = when (this) {
        is StackSignal.Bottom -> "$"
        is StackSignal.Symbol -> value.toString()
        is StackSignal.NodeView -> name
        is StackSignal.Marker -> "⁅$name⁆"
    }

    override fun SemanticAction<*>?.format() = if (this == null) "" else "\uD835\uDF06(${name})"

    fun Guard.Meaningful<*>.combinedPushFormat() = when (this) {
        is Guard.Input -> "${stackPushBefore.format()}|${stackPushAfter.format()}"
        is Guard.Stack -> "${stackPushBefore.format()}|${rollupTarget.format()}|${stackPushAfter.format()}"
    }
}