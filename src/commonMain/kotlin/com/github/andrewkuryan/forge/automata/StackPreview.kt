package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.endsWith

sealed class StackFrame {
    data object Botttom : StackFrame() {
        override fun toString() = "$"
    }
}

data class StackLetter(val name: String) : StackFrame() {
    override fun toString() = name
}

data class StackPreview(val frames: List<StackFrame>) {
    companion object {
        val ANY = StackPreview(listOf())
    }

    val isAny: Boolean get() = this == ANY

    operator fun plus(frame: StackFrame) = StackPreview(frames + frame)
    operator fun plus(other: StackPreview) = StackPreview(frames + other.frames)

    fun endsWith(other: StackPreview) = frames.endsWith(other.frames)

    override fun toString() = if (isAny) "*" else frames.joinToString("")
}