package com.github.andrewkuryan.forge.codegen.kt

import kotlin.reflect.KClass
import com.github.andrewkuryan.forge.automata.Guard
import com.github.andrewkuryan.forge.automata.MeaningfulTransition
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.format.format
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode

fun NSA<*>.generatePEGTable(nodeType: KClass<*> = SyntaxNode::class) =
    """val initState = ${initState.format(NSACodegen)}
    |val finalStates = setOf(${finalStates.joinToString(",") { it.format(NSACodegen) }})
    |val transitions = mapOf<State, List<Pair<Guard.Meaningful<${nodeType.simpleName}>, State>>>(
    |${generateTransitions()}
    |)""".trimMargin()

private fun NSA<*>.generateTransitions() = states
    .map { it to getOutTransitions(it).sortedWith(TransitionComparator) }
    .filter { it.second.isNotEmpty() }
    .joinToString(",\n") { (source, transitions) ->
        "\t${source.format(NSACodegen)} to listOf" + transitions
            .joinToString(",\n\t\t", "(\n\t\t", "\n\t)") {
                "Pair(${it.guard.format(NSACodegen)},${it.target.format(NSACodegen)})"
            }
    }

private object TransitionComparator : Comparator<MeaningfulTransition<*>> {

    override fun compare(a: MeaningfulTransition<*>, b: MeaningfulTransition<*>) =
        when (a.guard) {
            is Guard.Input -> when (b.guard) {
                is Guard.Input -> b.guard.inputSize - a.guard.inputSize
                is Guard.Stack -> -1
            }

            is Guard.Stack -> when (b.guard) {
                is Guard.Input -> 1
                is Guard.Stack -> b.guard.stackSize - a.guard.stackSize
            }
        }
}