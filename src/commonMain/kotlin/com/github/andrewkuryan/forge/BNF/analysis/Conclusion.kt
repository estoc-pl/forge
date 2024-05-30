package com.github.andrewkuryan.forge.BNF.analysis

import com.github.andrewkuryan.forge.BNF.Grammar
import com.github.andrewkuryan.forge.BNF.getGroupedDerivations

enum class ConclusionSeverity { INFO, WARNING, ERROR }

enum class FormatPattern { SHORT, DETAILED }

sealed class Conclusion(
    val severity: ConclusionSeverity,
    val title: String,
    val details: String,
    val description: String,
) {
    fun format(pattern: FormatPattern = FormatPattern.SHORT) = when (pattern) {
        FormatPattern.SHORT -> "$severity: $title"
        FormatPattern.DETAILED -> "$severity: $details. $description"
    }
}

fun analyze(grammar: Grammar): List<Conclusion> = grammar.getGroupedDerivations()
    .let { derivations ->
        listOfNotNull(
            hasUnresolvedRecursions(derivations),
            hasLeftRecursions(derivations)
        )
    }