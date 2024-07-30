package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.SeparatedSignals
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import cc.suffro.bpmanalyzer.isPowerOfTwo
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.nio.file.Path

class CombFilterOperationsImpl(private val combFilter: CombFilter) : CombFilterOperations {
    private val cache = mutableMapOf<Path, FFTData>()

    override fun getBpm(
        bassSignal: Signal,
        samplingRate: Int,
        params: CombFilterAnalyzerParams,
    ) = combFilter.getBpm(bassSignal, samplingRate, params)

    override fun getStartingPosition(
        searchDuration: Int,
        stepSize: Int,
        bassSignal: Signal,
        samplingRate: Int,
        bpm: Double,
    ) = combFilter.getStartingPosition(searchDuration, stepSize, bassSignal, samplingRate, bpm)

    override fun getFilledFilter(
        length: Int,
        bpm: Double,
        samplingRate: Int,
    ) = combFilter.getFilledFilter(length, bpm, samplingRate)

    override fun getFrequencyBands(fftData: FFTData): List<TimeDomainWindow> {
        val separatedSignals = Filterbank.separateSignals(fftData, MAXIMUM_FREQUENCY)
        return transformToTimeDomain(separatedSignals, fftData.duration).toList()
    }

    override fun getBassBand(fftData: FFTData): TimeDomainWindow = getFrequencyBands(fftData).first()

    override fun transformToTimeDomain(
        separatedSignals: SeparatedSignals,
        interval: Double,
    ): Sequence<TimeDomainWindow> {
        // TODO: add better handling for low frequencies, don't cut information
        val signalInTimeDomain =
            FFTProcessor.processInverse(
                separatedSignals.values.asSequence().map {
                    val powerOfTwo = getHighestPowerOfTwo(it.size)
                    it.asSequence().take(powerOfTwo)
                },
            )

        return signalInTimeDomain.mapIndexed { index, samples -> TimeDomainWindow(samples, interval, index * interval) }
    }

    override fun getRelevantSamples(
        bpm: Double,
        samplingRate: Int,
        signal: DoubleArray,
    ): List<Double> = combFilter.getRelevantSamples(bpm, samplingRate, signal)

    override fun calculateFftResult(
        wav: Wav,
        window: TimeDomainWindow,
        windowFunction: WindowFunction?,
    ): FFTData {
        return cache.getOrPut(wav.filePath) {
            FFTProcessor.process(window, wav.sampleRate, windowFunction = windowFunction)
        }
    }

    override fun calculateFftResult(
        signal: List<Double>,
        samplingRate: Int,
        filePath: Path,
        start: Double,
        windowFunction: WindowFunction?,
    ): FFTData {
        require(isPowerOfTwo(signal.size)) { "Signal size must be a power of two" }
        return cache.getOrPut(filePath) {
            FFTProcessor.process(signal, samplingRate, windowFunction = windowFunction)
        }
    }

    companion object {
        private const val MAXIMUM_FREQUENCY = 4096
    }
}
