package cc.suffro.bpmanalyzer.fft.data

import kotlin.math.PI
import kotlin.math.cos

typealias WindowFunction = (Int, Int) -> Double

enum class WindowFunctionType(val function: WindowFunction) {
    HAMMING(::hammingFunction),
    BLACKMAN(::blackmanFunction),
    HANNING(::hanningFunction),
}

fun hammingFunction(
    i: Int,
    length: Int,
): Double {
    val a = 25.0 / 46.0
    return a - (1 - a) * cos(2.0 * PI * i / length.toDouble())
}

fun blackmanFunction(
    i: Int,
    length: Int,
): Double {
    val a0 = 0.42
    val a1 = 0.5
    val a2 = 0.08
    return a0 - a1 * cos(2.0 * PI * i / length.toDouble()) + a2 * cos(4.0 * PI * i / length.toDouble())
}

fun hanningFunction(
    i: Int,
    length: Int,
): Double {
    val a = 0.5
    return a * (1 - cos(2 * PI * i / length))
}
