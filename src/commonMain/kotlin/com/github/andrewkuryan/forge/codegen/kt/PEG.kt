package com.github.andrewkuryan.forge.codegen.kt

import kotlin.reflect.KClass
import com.github.andrewkuryan.forge.parserKit.transition.*
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.format.format

private const val CONTAINER_ARG_NAME = "container"

fun NSA<*>.generatePEGTable(nodeType: KClass<*> = EmptyNode::class, containerType: KClass<*> = Unit::class) =
    NSACodegen(::formatSemanticAction).let { nsaCodegen ->
        """PEGTable<${nodeType.simpleName}, ${containerType.simpleName}>(
        |   ${initState.format(nsaCodegen)},
        |   setOf(${finalStates.joinToString(",") { it.format(nsaCodegen) }}),
        |) { $CONTAINER_ARG_NAME -> mapOf(
        |   ${generateTransitions(nsaCodegen)}
        |) }""".trimMargin()
    }

private fun NSA<*>.generateTransitions(nsaCodegen: NSACodegen) = states
    .map { it to getOutTransitions(it).sortedWith(TransitionComparator) }
    .filter { it.second.isNotEmpty() }
    .joinToString(",\n") { (source, transitions) ->
        "\t${source.format(nsaCodegen)} to listOf" + transitions
            .joinToString(",\n\t\t", "(\n\t\t", "\n\t)") {
                "Pair(${it.guard.format(nsaCodegen)},${it.target.format(nsaCodegen)})"
            }
    }

private fun formatSemanticAction(action: SemanticAction<*, *>?) = when (action) {
    is SemanticAction.Value -> "null"
    is SemanticAction.Ref -> "$CONTAINER_ARG_NAME.${action.path}"
    null -> "null"
}

object TransitionComparator : Comparator<MeaningfulTransition<*>> {

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