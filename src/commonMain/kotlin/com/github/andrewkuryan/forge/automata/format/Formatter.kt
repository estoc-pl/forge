package com.github.andrewkuryan.forge.automata.format

import kotlin.reflect.KClass
import com.github.andrewkuryan.forge.parserKit.transition.*
import com.github.andrewkuryan.forge.automata.*
import kotlin.jvm.JvmName

interface Formatter {

    fun NSA<*>.format(nodeType: KClass<*> = EmptyNode::class): String
    fun State.format(): String

    fun MeaningfulTransition<*>.format(): String
    fun Guard.Meaningful<*>.format(): String

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatInputSlice")
    fun InputSlice.format(): String

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatStackSlice")
    fun StackSlice.format(): String

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("formatStackPush")
    fun StackPush.format(): String

    fun InputSignal.format(): String
    fun StackSignal.Preview.format(): String
}

fun NSA<*>.format(formatter: Formatter = DefaultFormatter, nodeType: KClass<*> = EmptyNode::class) =
    with(formatter) { format(nodeType) }

fun State.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

fun MeaningfulTransition<*>.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }
fun Guard.Meaningful<*>.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

@JvmName("formatInputSlice")
fun InputSlice.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

@JvmName("formatStackSlice")
fun StackSlice.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

@JvmName("formatStackPush")
fun StackPush.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }

fun InputSignal.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }
fun StackSignal.Preview.format(formatter: Formatter = DefaultFormatter) = with(formatter) { format() }