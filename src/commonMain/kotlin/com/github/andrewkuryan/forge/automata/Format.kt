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
        |   Q = ${(transitionTable.keys + finalStates).joinToString(", ", "{", "}")}
        |   ẟ = ${
    transitionTable.entries.flatMap { entry -> entry.value }
        .joinToString(",\n", "{\n", "\n\t}") { transition -> "\t\t${transition.defaultFormat()}" }
}
        |   q₀ = $initState
        |   F = ${finalStates.joinToString(", ", "{", "}")}
        |)""".trimMargin()

fun NSA<*>.vizFormat() = """digraph {
        |   rankdir=LR;
        |   ${finalStates.joinToString(";\n") { "node [shape = doublecircle] \"${it}\";" }}
        |   node [shape = circle];
        |   secret_node [style=invis, shape=point];
        |   secret_node -> "$initState" [style=bold];
        |   ${
    transitionTable.entries.flatMap { entry -> entry.value }
        .joinToString("\n") { transition -> "\t${transition.vizFormat()}" }
}
        |}""".trimMargin()

inline fun <reified N : SyntaxNode> NSA<N>.ktSourceFormat() = """val initState = ${initState.ktSourceFormat()}
    |val finalStates = listOf(${finalStates.joinToString(",") { it.ktSourceFormat() }})
    |val transitions = mapOf(
    |${transitionTable.entries.joinToString(",\n") { it.ktSourceFormat() }}
    |)
""".trimMargin()

inline fun <reified N : SyntaxNode> Map.Entry<State, Set<Transition<N>>>.ktSourceFormat(): String {
    return "\t${this.key.ktSourceFormat()} to setOf(" +
            this.value.joinToString(",\n\t\t", "\n\t\t", "\n") { it.ktSourceFormat() } +
            "\t)"
}

fun Transition<*>.defaultFormat() = when (this) {
    is InputTransition -> "$source -> $input⟨$inputPreview⟩, ⟨$stackPreview⟩ / $stackPush -> $target"
    is StackTransition -> "$source -> ⟨$inputPreview⟩, $stack⟨$stackPreview⟩ / $stackPush -> $target"
    is EmptyTransition -> "$source -> $target"
}

fun Transition<*>.vizFormat() = when (this) {
    is InputTransition -> """"$source" -> "$target" [label=<$input⟨$inputPreview⟩ / ⟨$stackPreview⟩<br/>$stackPush>]"""
    is StackTransition -> """"$source" -> "$target" [label=<⟨$inputPreview⟩ / $stack⟨$stackPreview⟩<br/>$stackPush>]"""
    is EmptyTransition -> """"$source" -> "$target""""
}

inline fun <reified N : SyntaxNode> Transition<N>.ktSourceFormat() = when (this) {
    is InputTransition -> "InputTransition<${N::class.simpleName}>(" +
            "${input.ktSourceFormat()}," +
            "${stackPush.ktSourceFormat()}," +
            "${inputPreview.ktSourceFormat()}," +
            "${stackPreview.ktSourceFormat()}," +
            "${source.ktSourceFormat()}," +
            "${target.ktSourceFormat()})"

    is StackTransition -> "StackTransition<${N::class.simpleName}>(" +
            "${stack.ktSourceFormat()}," +
            "${stackPush.ktSourceFormat()}," +
            "${semanticAction.ktSourceFormat()}," +
            "${inputPreview.ktSourceFormat()}," +
            "${stackPreview.ktSourceFormat()}," +
            "${source.ktSourceFormat()}," +
            "${target.ktSourceFormat()})"

    is EmptyTransition -> "EmptyTransition<${N::class.simpleName}>(${source.ktSourceFormat()},${target.ktSourceFormat()})"
}

fun State.ktSourceFormat() = "State(${this.index})"

fun InputSlice.ktSourceFormat() =
    if (this.isEmpty) "InputSlice.EMPTY"
    else "InputSlice(listOf(${this.value.joinToString(",") { it.ktSourceFormat() }}))"

fun InputSignal.ktSourceFormat() = when (this) {
    is InputSignal.EOI -> "InputSignal.EOI"
    is InputSignal.Letter -> "InputSignal.Letter('${this.value}')"
}

fun StackSlice.ktSourceFormat() =
    if (this.isEmpty) "StackSlice.EMPTY"
    else "StackSlice(listOf(${this.value.joinToString(",") { it.ktSourceFormat() }}))"

fun StackSignal.ktSourceFormat() = when (this) {
    is StackSignal.Bottom -> "StackSignal.Bottom"
    is StackSignal.Letter -> "StackSignal.Letter(\"${this.name}\")"
}

inline fun <reified N : SyntaxNode> SemanticAction<N>?.ktSourceFormat() =
    if (this == null) "null"
    else "SemanticAction<${N::class.simpleName}>(\"${this.name}\",::${this.name})"