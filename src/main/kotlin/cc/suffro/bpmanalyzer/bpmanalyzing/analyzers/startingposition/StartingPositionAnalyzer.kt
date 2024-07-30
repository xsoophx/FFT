package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilterOperations
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import mu.KotlinLogging
import java.nio.file.Path

class StartingPositionAnalyzer(
    private val analyzer: Analyzer<Wav, TrackInfo>,
    private val wavReader: FileReader<Wav>,
    private val combFilterOperations: CombFilterOperations,
    private val fftProcessor: FFTProcessor,
) : CacheAnalyzer<Wav, StartingPosition> {
    override fun analyze(data: Wav): StartingPosition {
        val trackInfo = analyzer.analyze(data)
        return analyze(data, trackInfo.bpm)
    }

    override fun getPathAndAnalyze(path: String): StartingPosition {
        return getPathAndAnalyze(Path.of(path))
    }

    override fun getPathAndAnalyze(path: Path): StartingPosition {
        val wav = wavReader.read(path)
        val trackInfo = analyzer.analyze(wav)
        return analyze(wav, trackInfo.bpm)
    }

    override fun analyze(
        data: Wav,
        params: AnalyzerParams,
    ): StartingPosition {
        val bpm = (params as StartingPositionCacheAnalyzerParams).bpm
        return analyze(data, bpm)
    }

    private fun analyze(
        data: Wav,
        bpm: Bpm,
        samplesToSkip: Int = 0,
    ): StartingPosition {
        logger.info { "Analyzing starting position of track: ${data.filePath} with bpm: $bpm" }
        val sampleSizeToAnalyze = (ANALYZING_DURATION * data.sampleRate).toInt()
        val samples = data.defaultChannel().drop(samplesToSkip)

        val fftResults =
            samples.take(sampleSizeToAnalyze)
                .asSequence()
                .windowed(FFT_SAMPLES, STEP_SIZE, partialWindows = false) {
                    fftProcessor.process(it, data.sampleRate).magnitudes.sum()
                }.toList()

        val max = fftResults.max()
        val relatives = fftResults.map { it / max }
        val firstPeak = relatives.indexOfFirst { it > 0.1 }

        return StartingPosition(
            firstSample = firstPeak * STEP_SIZE,
            startInSec = firstPeak * STEP_SIZE / data.sampleRate.toDouble(),
        )
    }

    companion object {
        const val ANALYZING_DURATION = 2.0
        const val FFT_SAMPLES = 1024
        const val STEP_SIZE = 512
        private val logger = KotlinLogging.logger {}
    }
}
