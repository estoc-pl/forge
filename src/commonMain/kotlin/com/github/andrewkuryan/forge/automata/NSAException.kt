package com.github.andrewkuryan.forge.automata

open class NSAFormatException(message: String) : Exception(message)

class MultipleInitStatesException : NSAFormatException("Cannot have more than one initial state")