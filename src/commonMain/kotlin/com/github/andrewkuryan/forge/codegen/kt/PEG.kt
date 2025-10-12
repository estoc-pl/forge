package com.github.andrewkuryan.forge.codegen.kt

import kotlin.reflect.KClass
import com.github.andrewkuryan.forge.parserKit.transition.*
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.format.format

fun NSA<*>.generatePEGTable(nodeType: KClass<*> = EmptyNode::class) =
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
        when (val aGuard = a.guard) {
            is Guard.Input -> when (val bGuard = b.guard) {
                is Guard.Stack -> -1
                is Guard.Input -> InputSliceComparator.compare(
                    aGuard.input + aGuard.inputPreview,
                    bGuard.input + bGuard.inputPreview
                )
            }

            is Guard.Stack -> when (b.guard) {
                is Guard.Input -> 1
                is Guard.Stack -> b.guard.stackSize - a.guard.stackSize
            }
        }
}

private object InputSliceComparator : Comparator<InputSlice> {

    override fun compare(a: InputSlice, b: InputSlice): Int =
        (0 until maxOf(a.size, b.size)).fold(0) { result, index ->
            if (result != 0) result
            else b.getOrNull(index).getSortRank() - a.getOrNull(index).getSortRank()
        }
}

private fun InputSignal?.getSortRank() = when (this) {
    is InputSignal.EOI -> 4
    is InputSignal.Symbol -> 3
    is InputSignal.Range -> 2
    is InputSignal.Not -> 1
    null -> 0
}