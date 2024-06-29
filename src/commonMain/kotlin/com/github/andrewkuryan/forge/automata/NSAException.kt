package com.github.andrewkuryan.forge.automata

open class NSAFormatException(message: String) : Exception(message)

class UnreachableFinalStateException(state: State) :
    NSAFormatException("The $state is unreachable and cannot be marked as final")

class ReachableStateRemoveAttemptException(state: State) :
    NSAFormatException("The $state is reachable and cannot be safely removed")