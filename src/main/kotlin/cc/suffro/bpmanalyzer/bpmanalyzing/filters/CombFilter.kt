package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.abs
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.round
import mu.KotlinLogging
import kotlin.math.pow

class CombFilter {
    fun getBpm(
        bassSignal: Signal,
        samplingRate: Int,
        params: CombFilterAnalyzerParams,
    ): Double {
        val fftResult = FFTProcessor.process(bassSignal, samplingRate)
        val result = getBpm(params.minimumBpm, params.maximumBpm, params.stepSize, bassSignal, samplingRate, fftResult)

        return if (params.refinementParams == null) {
            result
        } else {
            val newMinimumBpm = result - params.refinementParams.deviationBpm
            val newMaximumBpm = result + params.refinementParams.deviationBpm
            getBpm(newMinimumBpm, newMaximumBpm, params.refinementParams.stepSize, bassSignal, samplingRate, fftResult)
        }
    }

    fun getStartingPosition(
        searchDuration: Int = 2,
        stepSize: Int,
        bassSignal: Signal,
        samplingRate: Int,
        bpm: Double,
    ): Pair<Int, Double> {
        var maxEnergy = 0.0
        var firstBeatPosition = 0.0

        val interval = (60.0 / bpm * samplingRate).toInt()
        val searchEnd = searchDuration * samplingRate

        for (position in 0..searchEnd step stepSize) {
            var energy = 0.0
            val pulses = MutableList(bassSignal.count()) { 0.0 }

            for (i in position until bassSignal.count() step interval) {
                if (i < bassSignal.count()) {
                    pulses[i] = 1.0
                }
            }

            val fftOfFilter = FFTProcessor.process(pulses.asSequence(), samplingRate)
            val fftResult = FFTProcessor.process(bassSignal, samplingRate)

            val convolution = (fftResult.output zip fftOfFilter.output).map { abs(it.first * it.second).pow(2) }
            energy += convolution.sum()

            if (energy > maxEnergy) {
                firstBeatPosition = position.toDouble()
                maxEnergy = energy
            }
        }

        return 0 to firstBeatPosition
    }

    private fun getBpm(
        minimumBpm: Double,
        maximumBpm: Double,
        stepSize: Double,
        bassSignal: Signal,
        samplingRate: Int,
        fftResult: FFTData,
    ): Double {
        var maxEnergy = 0.0
        var estimatedBpm = 0.0
        var bpm: Double = minimumBpm

        while (bpm <= maximumBpm) {
            var energy = 0.0
            val pulses = MutableList(bassSignal.count()) { 0.0 }
            fillPulses(bpm, pulses, samplingRate)
            val fftOfFilter = FFTProcessor.process(pulses.asSequence(), samplingRate)

            val convolution = (fftResult.output zip fftOfFilter.output).map { abs(it.first * it.second).pow(2) }
            energy += convolution.sum()

            logger.info { "$bpm BPM with energy: $energy." }
            if (energy > maxEnergy) {
                estimatedBpm = bpm
                maxEnergy = energy
            }
            bpm += stepSize
        }
        return estimatedBpm.round()
    }

    /*
     * Returns a list of pulses for the comb filter in relation to track BPM.
     */
    fun getFilledFilter(
        length: Int,
        bpm: Double,
        samplingRate: Int,
        pulses: Int = PULSES,
    ): List<Double> {
        val combLength = (pulses - 1) * length + 1
        val pulses =
            MutableList(combLength) { 0.0 }.also {
                fillPulses(bpm, it, samplingRate)
                it[0] = 1.0
            }

        return pulses
    }

    /*
     * Gets the first relevant samples of the signal used for analyzing the starting position via the comb filter.
     */
    fun getRelevantSamples(
        bpm: Double,
        samplingRate: Int,
        signal: DoubleArray,
    ): List<Double> {
        val step = (1.0 / bpm * 60 * samplingRate).toInt()
        val samples = signal.take(PULSES * step + 1)
        return samples
    }

    /*
     * Fills the pulses for the comb filter in relation to track BPM.
     */
    private fun fillPulses(
        bpm: Double,
        pulses: MutableList<Double>,
        samplingRate: Int,
        numberPulses: Int = PULSES,
    ) {
        val step = (1.0 / bpm * 60 * samplingRate).toInt()
        for (i in 0 until numberPulses) {
            pulses[i * step] = 1.0
        }
    }

    companion object {
        private const val PULSES = 3
        private val logger = KotlinLogging.logger {}
    }
}
