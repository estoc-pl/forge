package com.github.andrewkuryan.forge.extensions.grammar

import com.github.andrewkuryan.forge.automata.StackFrame
import kotlin.reflect.KFunction1

open class SyntaxNode

typealias SemanticHandler<N> = (body: List<StackFrame>) -> N
typealias SemanticFunction<N> = KFunction1<List<StackFrame>, N>

data class SemanticAction<N : SyntaxNode>(val name: String, val handler: SemanticHandler<N>)