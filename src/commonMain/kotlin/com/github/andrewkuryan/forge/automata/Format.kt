package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.grammar.SemanticAction
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode

enum class NSAFormatPattern { DEFAULT, VIZ, KT_SOURCE }

inline fun <reified N : SyntaxNode> NSA<N>.format(pattern: NSAFormatPattern) =
    when (pattern) {
        NSAFormatPattern.DEFAULT -> defaultFormat()
        NSAFormatPattern.VIZ -> vizFormat()
        NSAFormatPattern.KT_SOURCE -> ktSourceFormat()
    }

fun NSA<*>.defaultFormat() = """NSA(
    |    Q = ${states.joinToString(", ", "{", "}")}
    |    ẟ = ${states.flatMap(::getOutTransitions).joinToString(",\n\t\t", "{\n\t\t", "\n\t}") { it.defaultFormat() }}
    |    q₀ = $initState
    |    F = ${finalStates.joinToString(", ", "{", "}")}
    |)""".trimMargin()

fun NSA<*>.vizFormat() = """digraph {
    |    rankdir=LR;
    |${finalStates.joinToString(";\n\t", "\t", ";") { "node [shape = doublecircle] \"${it}\"" }}
    |    node [shape = circle];
    |    secret_node [style=invis, shape=point];
    |    secret_node -> "$initState" [style=bold];
    |${states.flatMap(::getOutTransitions).joinToString("\n\t", "\t") { it.vizFormat() }}
    |}""".trimMargin()

inline fun <reified N : SyntaxNode> NSA<N>.ktSourceFormat() = """val initState = ${initState.ktSourceFormat()}
    |val finalStates = listOf(${finalStates.joinToString(",") { it.ktSourceFormat() }})
    |val transitions = mapOf(
    |${states.map { it to getOutTransitions(it) }.joinToString(",\n") { it.ktSourceFormat() }}
    |)
""".trimMargin()

inline fun <reified N : SyntaxNode> Pair<State, List<MeaningfulTransition<N>>>.ktSourceFormat(): String {
    return "\t${this.first.ktSourceFormat()} to setOf(" +
            this.second.joinToString(",\n\t\t", "\n\t\t", "\n") { it.ktSourceFormat() } +
            "\t)"
}

fun Transition<*>.defaultFormat() = when (val transitionGuard = guard) {
    is Guard.Meaningful<*> -> "$source -> ${transitionGuard.defaultFormat()} -> $target"
    is Guard.Empty -> "$source -> $target"
}

fun Guard.Meaningful<*>.defaultFormat() = when (this) {
    is Guard.Input<*> -> "$input⟨$inputPreview⟩, ⟨$stackPreview⟩ / ${stackPushFormat()}"
    is Guard.Stack<*> -> "⟨$inputPreview⟩, $stack⟨$stackPreview⟩ / ${stackPushFormat()}"
}

fun Transition<*>.vizFormat() = when (val transitionGuard = guard) {
    is Guard.Meaningful<*> -> """"$source" -> "$target" ${transitionGuard.vizFormat()}"""
    is Guard.Empty -> """"$source" -> "$target""""
}

fun Guard.Meaningful<*>.vizFormat() = when (this) {
    is Guard.Input<*> -> "[label=<$input⟨$inputPreview⟩ / ⟨$stackPreview⟩<br/>${stackPushFormat()}>]"
    is Guard.Stack<*> -> "[label=<⟨$inputPreview⟩ / $stack⟨$stackPreview⟩<br/>${stackPushFormat()}>]"
}

fun Guard.Meaningful<*>.stackPushFormat() = when (this) {
    is Guard.Input -> "$stackPushBefore|$stackPushAfter"
    is Guard.Stack -> "$stackPushBefore|$rollupTarget|$stackPushAfter"
}

inline fun <reified N : SyntaxNode> Transition<N>.ktSourceFormat() = when (val transitionGuard = guard) {
    is Guard.Meaningful<*> -> "MeaningfulTransition<${N::class.simpleName}>(" +
            "${source.ktSourceFormat()}," +
            "${target.ktSourceFormat()}," +
            "${transitionGuard.ktSourceFormat()})"

    is Guard.Empty -> "EmptyTransition<${N::class.simpleName}>(${source.ktSourceFormat()},${target.ktSourceFormat()})"
}

fun Guard.Meaningful<*>.ktSourceFormat() = when (this) {
    is Guard.Input<*> -> "Guard.Input(" +
            "${input.ktSourceFormat()}," +
            "${inputPreview.ktSourceFormat()}," +
            "${stackPreview.ktSourceFormat()}," +
            "${stackPushBefore.ktSourceFormat()}," +
            "${stackPushAfter.ktSourceFormat()})"

    is Guard.Stack<*> -> "Guard.Stack(" +
            "${stack.ktSourceFormat()}," +
            "${rollupTarget.ktSourceFormat()}," +
            "${semanticAction.ktSourceFormat()}," +
            "${inputPreview.ktSourceFormat()}," +
            "${stackPreview.ktSourceFormat()}," +
            "${stackPushBefore.ktSourceFormat()}," +
            "${stackPushAfter.ktSourceFormat()})"
}

fun State.ktSourceFormat() = "State(${this.index})"

fun StackSlice.ktSourceFormat() =
    if (this.isEmpty) "StackSlice.EMPTY"
    else "StackSlice(listOf(${this.value.joinToString(",") { it.ktSourceFormat() }}))"

fun StackPush.ktSourceFormat() =
    if (this.isEmpty) "StackPush.EMPTY"
    else "StackPush(listOf(${this.value.joinToString(",") { it.ktSourceFormat() }}))"

fun StackSignal.Preview.ktSourceFormat() = when (this) {
    is StackSignal.Bottom -> "StackSignal.Bottom"
    is StackSignal.Symbol -> "StackSignal.Symbol('${this.value}')"
    is StackSignal.NodeView -> "StackSignal.NodeView(\"${this.name}\")"
    is StackSignal.Marker -> "StackSignal.Marker(\"${this.name}\")"
}

fun InputSlice.ktSourceFormat() =
    if (this.isEmpty) "InputSlice.EMPTY"
    else "InputSlice(listOf(${this.value.joinToString(",") { ktSourceFormatInputSignal(it) }}))"

fun ktSourceFormatBaseInputSignal(signal: InputSignal.Unitary) = when (signal) {
    is InputSignal.EOI -> "InputSignal.EOI"
    is InputSignal.Symbol -> "InputSignal.Symbol('${signal.value}')"
    is InputSignal.Range -> "InputSignal.Range('${signal.value.first}'..'${signal.value.last}')"
}

fun ktSourceFormatInputSignal(signal: InputSignal) = when (signal) {
    is InputSignal.Unitary -> ktSourceFormatBaseInputSignal(signal)
    is InputSignal.Not -> "InputSignal.Not(" +
            "${ktSourceFormatBaseInputSignal(signal.first)}," +
            "listOf(${signal.rest.joinToString(",") { ktSourceFormatBaseInputSignal(it) }}))"
}

fun SemanticAction<*>?.ktSourceFormat() =
    if (this == null) "null"
    else "SemanticAction(\"${this.name}\",::${this.name})"