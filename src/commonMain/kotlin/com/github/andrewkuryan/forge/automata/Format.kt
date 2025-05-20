package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.BNF.SemanticAction
import com.github.andrewkuryan.BNF.SyntaxNode

enum class NSAFormatPattern { DEFAULT, VIZ, KT_SOURCE }

inline fun <reified N : SyntaxNode> NSA<N>.format(pattern: NSAFormatPattern) =
    when (pattern) {
        NSAFormatPattern.DEFAULT -> defaultFormat()
        NSAFormatPattern.VIZ -> vizFormat()
        NSAFormatPattern.KT_SOURCE -> ktSourceFormat()
    }

fun NSA<*>.defaultFormat() = """NSA(
        |    Q = ${states.joinToString(", ", "{", "}")}
        |    ẟ = ${
    states.flatMap { state -> getOutTransitions(state) }
        .joinToString(",\n", "{\n", "\n\t}") { transition -> "\t\t${transition.defaultFormat()}" }
}
        |    q₀ = $initState
        |    F = ${finalStates.joinToString(", ", "{", "}")}
        |)""".trimMargin()

fun NSA<*>.vizFormat() = """digraph {
        |    rankdir=LR;
        |    ${finalStates.joinToString(";\n\t", postfix = ";") { "node [shape = doublecircle] \"${it}\"" }}
        |    node [shape = circle];
        |    secret_node [style=invis, shape=point];
        |    secret_node -> "$initState" [style=bold];
        |${
    states.flatMap { state -> getOutTransitions(state) }
        .joinToString("\n") { transition -> "\t${transition.vizFormat()}" }
}
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

fun MeaningfulTransition<*>.stackPushFormat() = when (this) {
    is InputTransition -> "$stackPushBefore|$stackPushAfter"
    is StackTransition -> "$stackPushBefore|$rollupTarget|$stackPushAfter"
}

fun Transition<*>.defaultFormat() = when (this) {
    is InputTransition -> "$source -> $input⟨$inputPreview⟩, ⟨$stackPreview⟩ / ${stackPushFormat()} -> $target"
    is StackTransition -> "$source -> ⟨$inputPreview⟩, $stack⟨$stackPreview⟩ / ${stackPushFormat()} -> $target"
    is EmptyTransition -> "$source -> $target"
}

fun Transition<*>.vizFormat() = when (this) {
    is InputTransition -> """"$source" -> "$target" [label=<$input⟨$inputPreview⟩ / ⟨$stackPreview⟩<br/>${stackPushFormat()}>]"""
    is StackTransition -> """"$source" -> "$target" [label=<⟨$inputPreview⟩ / $stack⟨$stackPreview⟩<br/>${stackPushFormat()}>]"""
    is EmptyTransition -> """"$source" -> "$target""""
}

inline fun <reified N : SyntaxNode> Transition<N>.ktSourceFormat() = when (this) {
    is InputTransition -> "InputTransition<${N::class.simpleName}>(" +
            "${input.ktSourceFormat()}," +
            "${inputPreview.ktSourceFormat()}," +
            "${stackPreview.ktSourceFormat()}," +
            "${source.ktSourceFormat()}," +
            "${target.ktSourceFormat()}," +
            "${stackPushBefore.ktSourceFormat()}," +
            "${stackPushAfter.ktSourceFormat()})"

    is StackTransition -> "StackTransition<${N::class.simpleName}>(" +
            "${stack.ktSourceFormat()}," +
            "${rollupTarget.ktSourceFormat()}," +
            "${semanticAction.ktSourceFormat()}," +
            "${inputPreview.ktSourceFormat()}," +
            "${stackPreview.ktSourceFormat()}," +
            "${source.ktSourceFormat()}," +
            "${target.ktSourceFormat()}," +
            "${stackPushBefore.ktSourceFormat()}," +
            "${stackPushAfter.ktSourceFormat()})"

    is EmptyTransition -> "EmptyTransition<${N::class.simpleName}>(${source.ktSourceFormat()},${target.ktSourceFormat()})"
}

fun State.ktSourceFormat() = "State(${this.index})"

fun StackSlice.ktSourceFormat() =
    if (this.isEmpty) "StackSlice.EMPTY"
    else "StackSlice(listOf(${this.value.joinToString(",") { it.ktSourceFormat() }}))"

fun StackPush.ktSourceFormat() =
    if (this.isEmpty) "StackPush.EMPTY"
    else "StackPush(listOf(${this.value.joinToString(",") { it.ktSourceFormat() }}))"

fun StackSignal.ktSourceFormat() = when (this) {
    is StackSignal.Bottom -> "StackSignal.Bottom"
    is StackSignal.Symbol -> "StackSignal.Symbol('${this.value}')"
    is StackSignal.Node -> "StackSignal.Node(\"${this.name}\")"
    is StackSignal.Marker -> "StackSignal.Marker(\"${this.name}\")"
}

fun InputSlice.ktSourceFormat() =
    if (this.isEmpty) "InputSlice.EMPTY"
    else "InputSlice(listOf(${this.value.joinToString(",") { ktSourceFormatInputSignal(it) }}))"

fun ktSourceFormatBaseInputSignal(signal: BaseInputSignal) = when (signal) {
    is InputSignal.EOI -> "InputSignal.EOI"
    is InputSignal.Symbol -> "InputSignal.Symbol('${signal.value}')"
    is InputSignal.Range -> "InputSignal.Range('${signal.value.first}'..'${signal.value.last}')"
}

fun ktSourceFormatInputSignal(signal: InputSignal) = when (signal) {
    is BaseInputSignal -> ktSourceFormatBaseInputSignal(signal)
    is InputSignal.Not -> "InputSignal.Not(" +
            "${ktSourceFormatBaseInputSignal(signal.first)}," +
            "listOf(${signal.rest.joinToString(",") { ktSourceFormatBaseInputSignal(it) }}))"
}

inline fun <reified N : SyntaxNode> SemanticAction<N>?.ktSourceFormat() =
    if (this == null) "null"
    else "SemanticAction<${N::class.simpleName}>(\"${this.name}\",::${this.name})"