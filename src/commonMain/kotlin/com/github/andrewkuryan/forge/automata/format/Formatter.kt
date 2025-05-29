package com.github.andrewkuryan.forge.automata.format

import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.grammar.SemanticAction
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode
import kotlin.reflect.KClass

interface Formatter {

    fun NSA<*>.format(nodeType: KClass<*> = SyntaxNode::class): String
    fun State.format(): String

    fun MeaningfulTransition<*>.format(): String
    fun Guard.Meaningful<*>.format(): String

    fun InputSlice.format(): String
    fun StackSlice.format(): String
    fun StackPush.format(): String

    fun InputSignal.format(): String
    fun StackSignal.Preview.format(): String

    fun SemanticAction<*>?.format(): String
}

fun NSA<*>.format(formatter: Formatter = DefaultFormatter, nodeType: KClass<*> = SyntaxNode::class) =
    with(formatter) { format(nodeType) }

fun State.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

fun MeaningfulTransition<*>.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }
fun Guard.Meaningful<*>.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

fun InputSlice.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }
fun StackSlice.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }
fun StackPush.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

fun InputSignal.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }
fun StackSignal.Preview.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

fun SemanticAction<*>?.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }