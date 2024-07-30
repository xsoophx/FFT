package cc.suffro.bpmanalyzer

import org.kotlinmath.Complex
import org.kotlinmath.sqrt
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun abs(n: Complex): Double = sqrt(n.re * n.re + n.im * n.im).re

// calculates the highest power of two that is less than or equal to the given number
fun getHighestPowerOfTwo(number: Int): Int {
    var result = number
    generateSequence(1) { (it * 2) }
        .take(5)
        .forEach { position -> result = result or (result shr position) }

    return result xor (result shr 1)
}

fun isPowerOfTwo(number: Int): Boolean = number and (number - 1) == 0

data class Interval<T>(
    val lowerBound: T,
    val upperBound: T,
)

fun Double.round(pattern: String = "#.##"): Double =
    DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        .apply {
            roundingMode = RoundingMode.CEILING
        }
        .format(this)
        .toDouble()
