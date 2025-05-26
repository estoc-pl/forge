package com.github.andrewkuryan.forge.extensions.grammar

import com.github.andrewkuryan.forge.automata.StackSignal
import kotlin.reflect.KFunction1

open class SyntaxNode

typealias SemanticHandler<N> = (body: List<StackSignal.Read>) -> N?
typealias SemanticFunction<N> = KFunction1<List<StackSignal.Read>, N?>

data class SemanticAction<N : SyntaxNode>(val name: String, val handler: SemanticHandler<N>)