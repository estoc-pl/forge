package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.*

sealed class TransitionBody {
    abstract val inputPreview: InputSlice
    abstract val stackPreview: StackSlice
}

data class InputTransitionBody(
    val input: InputSlice,
    override val inputPreview: InputSlice,
    override val stackPreview: StackSlice,
    val stackPush: StackSlice,
) : TransitionBody() {

    override fun toString() = "$input⟨$inputPreview⟩, ⟨$stackPreview⟩ / $stackPush"
}

data class StackTransitionBody(
    val stack: StackSlice,
    override val inputPreview: InputSlice,
    override val stackPreview: StackSlice,
    val stackPush: StackSignal,
) : TransitionBody() {

    override fun toString() = "⟨$inputPreview⟩, $stack⟨$stackPreview⟩ / $stackPush"
}

fun TransitionBody.isSameAs(transition: Transition<*>) =
    when {
        this is InputTransitionBody && transition is InputTransition ->
            input == transition.input && stackPush == transition.stackPush &&
                    inputPreview == transition.inputPreview && stackPreview == transition.stackPreview

        this is StackTransitionBody && transition is StackTransition ->
            stack == transition.stack && stackPush == transition.stackPush &&
                    inputPreview == transition.inputPreview && stackPreview == transition.stackPreview

        else -> false
    }

fun read(input: Char, stackPreview: String) =
    InputTransitionBody(
        InputSlice(listOf(NSASignal.Symbol(input))),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice(listOf(NSASignal.Symbol(input)))
    )

fun read(input: String, stackPreview: String) =
    InputTransitionBody(
        InputSlice(parseInputSignals(input)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice(parseStackSignals(input))
    )

fun rollup(stackPreview: String, stack: String, target: String) =
    StackTransitionBody(
        StackSlice(parseStackSignals(stack)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSignal.Node(target)
    )

fun exit(stackPreview: String) =
    InputTransitionBody(
        InputSlice(listOf(InputSignal.EOI)),
        InputSlice.EMPTY,
        StackSlice(parseStackSignals(stackPreview)),
        StackSlice.EMPTY,
    )

private typealias Transformer<T> = Pair<Regex, (IntRange, String) -> T>

private val RANGE_TRANSFORMER: Transformer<NSASignal.Range> =
    Regex(".-.") to { range, input -> NSASignal.Range(input[range.first]..input[range.last]) }
private val SINGLE_NOT_TRANSFORMER: Transformer<NSASignal.Not> =
    Regex("\\^[^\\[]") to { range, input -> NSASignal.Not(NSASignal.Symbol(input[range.first + 1])) }
private val COMPLEX_NOT_TRANSFORMER: Transformer<NSASignal.Not> = Regex("\\^\\[..+]") to { range, input ->
    val nestedSignals = parseBaseNSASignals(input.substring(range.first + 2 until range.last))
    NSASignal.Not(nestedSignals.first(), nestedSignals.slice(1 until nestedSignals.size))
}
private val EOI_TRANSFORMER: Transformer<InputSignal.EOI> =
    Regex(InputSignal.EOI.toString()) to { _, _ -> InputSignal.EOI }

private val STACK_NODE_TRANSFORMER: Transformer<StackSignal.Node> =
    Regex("([A-Z_]+[0-9]*)") to { range, input -> StackSignal.Node(input.substring(range)) }
private val STACK_BOTTOM_TRANSFORMER: Transformer<StackSignal.Bottom> =
    Regex("\\$") to { _, _ -> StackSignal.Bottom }

private val DEFAULT_TRANSFORMER = { symbol: Char -> NSASignal.Symbol(symbol) }

fun <T, D : T> parseSignals(transformers: List<Transformer<T>>, parseDefault: (Char) -> D): (String) -> List<T> {
    fun parseFragment(input: String, restTransformers: List<Transformer<T>>): List<T> {
        val (regex, transformerFn) = restTransformers.first()
        val foundSignals = regex.findAll(input)
            .map { it.range to transformerFn(it.range, input) }
            .toList()
            .toTypedArray()
        return listOf(IntRange(0, -1) to null, *foundSignals, IntRange(input.length, input.length - 1) to null)
            .zipWithNext()
            .map { (start, end) -> start.second to ((start.first.last + 1) until end.first.first) }
            .fold(listOf()) { acc, (symbol, nextRange) ->
                val nestedSymbols =
                    if (restTransformers.size > 1) parseFragment(input.substring(nextRange), restTransformers.drop(1))
                    else nextRange.map { parseDefault(input[it]) }
                acc + (if (symbol != null) listOf(symbol) else listOf()) + nestedSymbols
            }
    }

    return { input -> parseFragment(input, transformers) }
}

val parseBaseNSASignals = parseSignals(listOf<Transformer<BaseNSASignal>>(RANGE_TRANSFORMER), DEFAULT_TRANSFORMER)
val parseInputSignals = parseSignals(
    listOf(
        EOI_TRANSFORMER,
        SINGLE_NOT_TRANSFORMER,
        COMPLEX_NOT_TRANSFORMER,
        RANGE_TRANSFORMER
    ),
    DEFAULT_TRANSFORMER
)
val parseStackSignals = parseSignals(
    listOf(
        STACK_BOTTOM_TRANSFORMER,
        STACK_NODE_TRANSFORMER,
        SINGLE_NOT_TRANSFORMER,
        COMPLEX_NOT_TRANSFORMER,
        RANGE_TRANSFORMER
    ),
    DEFAULT_TRANSFORMER
)