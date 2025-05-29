package com.github.andrewkuryan.forge.automata.format

import kotlin.reflect.KClass
import com.github.andrewkuryan.forge.automata.*

object VizFormatter : Formatter by DefaultFormatter {

    override fun NSA<*>.format(nodeType: KClass<*>?) = """digraph {
    |    rankdir=LR;
    |${finalStates.joinToString(";\n\t", "\t", ";") { "node [shape = doublecircle] \"${it.format()}\"" }}
    |    node [shape = circle];
    |    secret_node [style=invis, shape=point];
    |    secret_node -> "${initState.format()}" [style=bold];
    |${states.flatMap(::getOutTransitions).joinToString("\n\t", "\t") { it.format() }}
    |}""".trimMargin()

    override fun MeaningfulTransition<*>.format(nodeType: KClass<*>?) =
        "\"${source.format()}\" -> \"${target.format()}\" ${guard.format()}"

    override fun Guard.Meaningful<*>.format() = when (this) {
        is Guard.Input -> "[label=<${input.format()}⟨${inputPreview.format()}⟩ / ⟨${stackPreview.format()}⟩<br/>${combinedPushFormat()}>]"
        is Guard.Stack -> "[label=<⟨${inputPreview.format()}⟩ / ${stack.format()}⟨${stackPreview.format()}⟩<br/>${combinedPushFormat()}>]"
    }

    private fun Guard.Meaningful<*>.combinedPushFormat() = with(DefaultFormatter) { format() }
}