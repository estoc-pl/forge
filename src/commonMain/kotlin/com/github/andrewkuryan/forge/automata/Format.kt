package com.github.andrewkuryan.forge.automata

enum class NSAFormatPattern { DEFAULT, VIZ }

fun NSA<*>.format(pattern: NSAFormatPattern) =
    when (pattern) {
        NSAFormatPattern.DEFAULT -> defaultFormat()
        NSAFormatPattern.VIZ -> vizFormat()
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

fun Transition<*>.defaultFormat() = "$source -> $input, $stackPreview / ${action ?: '-'} -> $target"
fun Transition<*>.vizFormat() = """"$source" -> "$target" [label=<$input / $stackPreview<br/>${action ?: '-'}>]"""