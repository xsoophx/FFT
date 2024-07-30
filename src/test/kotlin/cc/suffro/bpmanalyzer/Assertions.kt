@file:Suppress("ktlint:standard:filename")

package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import org.kotlinmath.Complex
import kotlin.math.abs
import kotlin.test.assertTrue

fun assertNearlyEquals(
    expected: Double,
    actual: Double,
    e: Double = 0.2,
    message: String? = null,
) {
    assertTrue(actual.closeTo(expected, e), message ?: "Expected <$expected>, actual <$actual>.")
}

fun assertNearlyEquals(
    expected: Complex,
    actual: Complex,
    e: Double = 2.0,
    message: String? = null,
) {
    assertTrue(actual.closeTo(expected, e), message ?: "Expected <$expected>, actual <$actual>.")
}

fun assertNearlyEquals(
    expected: Double,
    actual: Double,
    e: Int,
    exclusive: Boolean = true,
    message: String? = null,
) {
    assertTrue(actual.closeTo(expected, e, exclusive), message ?: "Expected <$expected>, actual <$actual>.")
}

fun assertNearlyEquals(
    expected: Int,
    actual: Int,
    e: Int,
    exclusive: Boolean = true,
    message: String? = null,
) {
    assertTrue(actual.closeTo(expected, e, exclusive), message ?: "Expected <$expected>, actual <$actual>.")
}

fun assertNearlyEquals(
    expected: StartingPosition,
    actual: StartingPosition,
    sampleDistance: Int = 100,
    secondDistance: Double = 0.01,
    message: String? = null,
) {
    assertTrue(
        actual.firstSample.closeTo(expected.firstSample, sampleDistance, true),
        message ?: "Expected <$expected>, actual <$actual>.",
    )
    assertTrue(
        actual.startInSec.closeTo(expected.startInSec, secondDistance),
        message ?: "Expected <$expected>, actual <$actual>.",
    )
}

private fun Complex.closeTo(
    number: Complex,
    e: Double,
): Boolean = abs(re - number.re) < e && abs(im - number.im) < e

private fun Double.closeTo(
    number: Double,
    e: Double,
): Boolean = abs(this - number) < e

private fun Int.closeTo(
    number: Int,
    e: Int,
    exclusive: Boolean,
): Boolean = if (exclusive) abs(this - number) < e else abs(this - number) <= e

private fun Double.closeTo(
    number: Double,
    e: Int,
    exclusive: Boolean,
): Boolean = if (exclusive) abs(this - number) < e else abs(this - number) <= e
