package com.github.andrewkuryan.forge.generator.BNFtoRD.regular

import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.generator.regular.RightRecTest
import com.github.andrewkuryan.forge.utils.NSAAssertion
import com.github.andrewkuryan.forge.utils.exit
import com.github.andrewkuryan.forge.utils.read
import com.github.andrewkuryan.forge.utils.rollup
import kotlin.test.Test

class RDRightRecTest : RightRecTest({ buildRDParser() }) {

    @Test
    override fun `should build NSA for S → aS ⏐ b`() =
        assertBuilding(`S → aS ⏐ b`) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[0],
                        read('b', "") to s[1]
                    ),
                    s[1] to listOf(rollup("", "b", "S") to s[2]),
                    s[2] to listOf(
                        rollup("", "Sa", "S") to s[2],
                        exit("S$") to s[3],
                    ),
                    s[3] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → aA；A → bB；B → cS ⏐ d`() =
        assertBuilding(`S → aA；A → bB；B → cS ⏐ d`) { s ->
            NSAAssertion(
                s[0], s[7],
                mapOf(
                    s[0] to listOf(read('a', "") to s[1]),
                    s[1] to listOf(read('b', "") to s[2]),
                    s[2] to listOf(
                        read('c', "") to s[0],
                        read('d', "") to s[3],
                    ),
                    s[3] to listOf(rollup("", "d", "B") to s[4]),
                    s[4] to listOf(rollup("", "Bb", "A") to s[5]),
                    s[5] to listOf(rollup("", "Aa", "S") to s[6]),
                    s[6] to listOf(
                        rollup("", "Sc", "B") to s[4],
                        exit("S$") to s[7]
                    ),
                    s[7] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → aS ⏐ A；A → bB；B → cS ⏐ d`() =
        assertBuilding(`S → aS ⏐ A；A → bB；B → cS ⏐ d`) { s ->
            NSAAssertion(
                s[0], s[6],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[0],
                        read('b', "") to s[1],
                    ),
                    s[1] to listOf(
                        read('c', "") to s[0],
                        read('d', "") to s[2],
                    ),
                    s[2] to listOf(rollup("", "d", "B") to s[3]),
                    s[3] to listOf(rollup("", "Bb", "A") to s[4]),
                    s[4] to listOf(rollup("", "A", "S") to s[5]),
                    s[5] to listOf(
                        rollup("", "Sa", "S") to s[5],
                        rollup("", "Sc", "B") to s[3],
                        exit("S$") to s[6]
                    ),
                    s[6] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → xA ⏐ y；A → aS ⏐ b`() =
        assertBuilding(`S → xA ⏐ y；A → aS ⏐ b`) { s ->
            NSAAssertion(
                s[0], s[6],
                mapOf(
                    s[0] to listOf(
                        read('x', "") to s[1],
                        read('y', "") to s[2]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[0],
                        read('b', "") to s[3]
                    ),
                    s[3] to listOf(rollup("", "b", "A") to s[4]),
                    s[4] to listOf(rollup("", "Ax", "S") to s[5]),
                    s[5] to listOf(
                        rollup("", "Sa", "A") to s[4],
                        exit("S$") to s[6]
                    ),
                    s[2] to listOf(rollup("", "y", "S") to s[5]),
                    s[6] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → iA ⏐ s；A → aS ⏐ bB；B → cA ⏐ d`() =
        assertBuilding(`S → iA ⏐ s；A → aS ⏐ bB；B → cA ⏐ d`) { s ->
            NSAAssertion(
                s[0], s[8],
                mapOf(
                    s[0] to listOf(
                        read('i', "") to s[1],
                        read('s', "") to s[2]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[0],
                        read('b', "") to s[3]
                    ),
                    s[3] to listOf(
                        read('c', "") to s[1],
                        read('d', "") to s[4]
                    ),
                    s[4] to listOf(rollup("", "d", "B") to s[5]),
                    s[5] to listOf(rollup("", "Bb", "A") to s[6]),
                    s[6] to listOf(
                        rollup("", "Ai", "S") to s[7],
                        rollup("", "Ac", "B") to s[5],
                    ),
                    s[7] to listOf(
                        rollup("", "Sa", "A") to s[6],
                        exit("S$") to s[8]
                    ),
                    s[2] to listOf(rollup("", "s", "S") to s[7]),
                    s[8] to listOf()
                )
            )
        }
}