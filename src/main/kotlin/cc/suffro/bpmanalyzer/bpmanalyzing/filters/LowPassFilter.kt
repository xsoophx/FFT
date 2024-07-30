package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.fft.data.hanningFunction
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import org.kotlinmath.Complex
import kotlin.math.abs
import kotlin.math.roundToInt

object LowPassFilter {
    fun process(
        window: TimeDomainWindow,
        sampleRate: Int,
    ): Signal {
        val fullWaveRectified = TimeDomainWindow(window.map(::abs), window.duration, window.startingTime)
        val numSamples = (window.duration * 2 * sampleRate).roundToInt()
        val halfHanningWindow = getHalfOfHanningWindow(numSamples)
        val (first, second) = processSignals(fullWaveRectified, halfHanningWindow, sampleRate)

        val convolved = convolve(first, second)
        return FFTProcessor.processInverse(convolved)
    }

    fun process(
        window: TimeDomainWindow,
        fmtChunk: FmtChunk,
    ) = process(window, fmtChunk.sampleRate)

    private fun processSignals(
        a: Sequence<Double>,
        b: Sequence<Double>,
        sampleRate: Int,
    ): Pair<List<Complex>, List<Complex>> {
        val size = getSmallerSizeOf(a.count(), b.count())
        val first = FFTProcessor.process(a.take(size), sampleRate).output
        val second = FFTProcessor.process(b.take(size), sampleRate).output
        return first to second
    }

    private fun convolve(
        a: List<Complex>,
        b: List<Complex>,
        block: (Pair<Complex, Complex>) -> Complex = { it.first * it.second },
    ): Sequence<Complex> {
        return a.zip(b).map(block).asSequence()
    }

    private fun getSmallerSizeOf(
        a: Int,
        b: Int,
    ): Int = minOf(getHighestPowerOfTwo(a), getHighestPowerOfTwo(b))

    private fun getHalfOfHanningWindow(numSamples: Int): Sequence<Double> =
        (0 until numSamples)
            .map { hanningFunction(it, numSamples) }
            .subList(numSamples / 2, numSamples)
            .asSequence()

    private fun FFTProcessor.processInverse(signal: Sequence<Complex>): Sequence<Double> = processInverse(sequenceOf(signal)).first()
}
