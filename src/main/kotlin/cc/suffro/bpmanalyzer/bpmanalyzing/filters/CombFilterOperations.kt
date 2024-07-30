package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.SeparatedSignals
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.nio.file.Path

interface CombFilterOperations {
    fun getBpm(
        bassSignal: Signal,
        samplingRate: Int,
        params: CombFilterAnalyzerParams,
    ): Double

    fun getStartingPosition(
        searchDuration: Int = 2,
        stepSize: Int,
        bassSignal: Signal,
        samplingRate: Int,
        bpm: Double,
    ): Pair<Int, Double>

    fun getFilledFilter(
        length: Int,
        bpm: Double,
        samplingRate: Int,
    ): List<Double>

    fun getFrequencyBands(fftData: FFTData): List<TimeDomainWindow>

    fun getBassBand(fftData: FFTData): TimeDomainWindow

    fun transformToTimeDomain(
        separatedSignals: SeparatedSignals,
        interval: Double,
    ): Sequence<TimeDomainWindow>

    fun getRelevantSamples(
        bpm: Double,
        samplingRate: Int,
        signal: DoubleArray,
    ): List<Double>

    fun calculateFftResult(
        wav: Wav,
        window: TimeDomainWindow,
        windowFunction: WindowFunction? = null,
    ): FFTData

    fun calculateFftResult(
        signal: List<Double>,
        samplingRate: Int,
        filePath: Path,
        start: Double = 0.0,
        windowFunction: WindowFunction? = null,
    ): FFTData
}
