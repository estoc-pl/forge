package com.github.andrewkuryan.forge.translation

typealias SemanticAction<N> = (head: N, body: List<N>) -> Unit

open class SyntaxNode

typealias NodeBuilder<N> = () -> N
