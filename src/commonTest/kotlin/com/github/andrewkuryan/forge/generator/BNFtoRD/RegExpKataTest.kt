package com.github.andrewkuryan.forge.generator.BNFtoRD

import kotlin.test.Test
import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.utils.*

/**
 *  Solution for the CodeWars kata: https://www.codewars.com/kata/5470c635304c127cad000f0dv
 **/
class RegExpKataTest {

    private val regExpGrammar = grammar {
        val E by nonterm()
        val E1 by nonterm()
        val E2 by nonterm()
        val E3 by nonterm()
        val E4 by nonterm()
        val N by nonterm()

        S /= E
        E /= E1..'|' / E1
        E1 /= E2..'|'..E2 / '|'..E2 / E2
        E2 /= E2..E3 / E3
        E3 /= E4..'*' / E4
        E4 /= N / '.' / '('..E..')'
        N /= regexp { !'(' * !')' * !'*' * !'|' * !'.' }
    }

    @Test
    fun `should build NSA for simplified regular expressions`() {
        val nsa = regExpGrammar.buildRDParser()

        val s = Array(15) { StateRef() }
        nsa.assertTransitions(
            s[0], s[14],
            mapOf(
                s[0] to listOf(
                    read('(', "") to s[0],
                    read('|', "") to s[1],
                    read("^[()*|.]", "", "⁅[^()*|.]⁆") to s[2],
                    read('.', "") to s[3],
                ),
                s[1] to listOf(
                    read('(', "") to s[0],
                    read("^[()*|.]", "", "⁅[^()*|.]⁆") to s[2],
                    read('.', "") to s[3],
                ),
                s[2] to listOf(rollup("", "⁅[^()*|.]⁆", "N") to s[4]),
                s[3] to listOf(rollup("", ".", "E4") to s[5]),
                s[4] to listOf(rollup("", "N", "E4") to s[5]),
                s[5] to listOf(
                    read('*', "") to s[6],
                    rollup("", "E4", "E3") to s[7]
                ),
                s[6] to listOf(rollup("", "*E4", "E3") to s[7]),
                s[7] to listOf(
                    rollup("", "E3E2", "E2") to s[8],
                    rollup("", "E3", "E2") to s[8],
                ),
                s[8] to listOf(
                    read('(', "") to s[0],
                    read('|', "") to s[1],
                    read("^[()*|.]", "", "⁅[^()*|.]⁆") to s[2],
                    read('.', "") to s[3],
                    rollup("", "E2", "E1") to s[9],
                    rollup("", "E2|", "E1") to s[9],
                    rollup("", "E2|E2", "E1") to s[9],
                ),
                s[9] to listOf(
                    read('|', "") to s[10],
                    rollup("", "E1", "E") to s[11]
                ),
                s[10] to listOf(rollup("", "|E1", "E") to s[11]),
                s[11] to listOf(
                    rollup("", "E", "S") to s[12],
                    read(')', "") to s[13]
                ),
                s[12] to listOf(exit("S$") to s[14]),
                s[13] to listOf(rollup("", ")E(", "E4") to s[5]),
                s[14] to listOf()
            )
        )
    }
}