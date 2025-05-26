package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode

fun read(input: Char, stackPreview: String, stackPushBefore: String = "", stackPushAfter: String = "") =
    Guard.Input<SyntaxNode>(
        input = InputSlice(listOf(InputSignal.Symbol(input))),
        stackPreview = StackSlice(parseStackSignals(stackPreview)),
        stackPushBefore = StackPush(parseStackPush(stackPushBefore)),
        stackPushAfter = StackPush(parseStackPush(stackPushAfter))
    )

fun read(input: String, stackPreview: String, stackPushBefore: String = "", stackPushAfter: String = "") =
    Guard.Input<SyntaxNode>(
        input = InputSlice(parseInputSignals(input)),
        stackPreview = StackSlice(parseStackSignals(stackPreview)),
        stackPushBefore = StackPush(parseStackPush(stackPushBefore)),
        stackPushAfter = StackPush(parseStackPush(stackPushAfter))
    )

fun rollup(
    stackPreview: String,
    stack: String,
    target: String,
    stackPushBefore: String = "",
    stackPushAfter: String = "",
) = Guard.Stack<SyntaxNode>(
    stack = StackSlice(parseStackSignals(stack)),
    rollupTarget = StackSignal.NodeView(target),
    stackPreview = StackSlice(parseStackSignals(stackPreview)),
    stackPushBefore = StackPush(parseStackPush(stackPushBefore)),
    stackPushAfter = StackPush(parseStackPush(stackPushAfter))
)

fun exit(stackPreview: String) =
    Guard.Input<SyntaxNode>(
        input = InputSlice(listOf(InputSignal.EOI)),
        stackPreview = StackSlice(parseStackSignals(stackPreview))
    )

private typealias Transformer<T> = Pair<Regex, (IntRange, String) -> T>

private val EOI_TRANSFORMER: Transformer<InputSignal.EOI> =
    Regex(InputSignal.EOI.toString()) to { _, _ -> InputSignal.EOI }
private val INPUT_SYMBOL_TRANSFORMER = { symbol: Char -> InputSignal.Symbol(symbol) }
private val RANGE_TRANSFORMER: Transformer<InputSignal.Range> =
    Regex(".-.") to { range, input -> InputSignal.Range(input[range.first]..input[range.last]) }
private val SINGLE_NOT_TRANSFORMER: Transformer<InputSignal.Not> =
    Regex("\\^[^\\[]") to { range, input -> InputSignal.Not(InputSignal.Symbol(input[range.first + 1])) }
private val COMPLEX_NOT_TRANSFORMER: Transformer<InputSignal.Not> = Regex("\\^\\[..+]") to { range, input ->
    val nestedSignals = parseUnitaryInputSignals(input.substring(range.first + 2 until range.last))
    InputSignal.Not(nestedSignals.first(), nestedSignals.slice(1 until nestedSignals.size))
}

private val STACK_BOTTOM_TRANSFORMER: Transformer<StackSignal.Bottom> =
    Regex("\\$") to { _, _ -> StackSignal.Bottom }
private val STACK_SYMBOL_TRANSFORMER = { symbol: Char -> StackSignal.Symbol(symbol) }
private val STACK_NODE_TRANSFORMER: Transformer<StackSignal.NodeView> =
    Regex("([A-Z_]+[0-9]*)") to { range, input -> StackSignal.NodeView(input.substring(range)) }
private val STACK_MARKER_TRANSFORMER: Transformer<StackSignal.Marker> =
    Regex("⁅.+⁆") to { range, input -> StackSignal.Marker(input.substring(range).drop(1).dropLast(1)) }

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

val parseUnitaryInputSignals = parseSignals(
    listOf(EOI_TRANSFORMER, RANGE_TRANSFORMER),
    INPUT_SYMBOL_TRANSFORMER
)
val parseInputSignals = parseSignals(
    listOf(EOI_TRANSFORMER, SINGLE_NOT_TRANSFORMER, COMPLEX_NOT_TRANSFORMER, RANGE_TRANSFORMER),
    INPUT_SYMBOL_TRANSFORMER
)
val parseStackSignals = parseSignals(
    listOf(STACK_BOTTOM_TRANSFORMER, STACK_MARKER_TRANSFORMER, STACK_NODE_TRANSFORMER),
    STACK_SYMBOL_TRANSFORMER
)
val parseStackPush = parseSignals(
    listOf(STACK_MARKER_TRANSFORMER),
    STACK_SYMBOL_TRANSFORMER
)