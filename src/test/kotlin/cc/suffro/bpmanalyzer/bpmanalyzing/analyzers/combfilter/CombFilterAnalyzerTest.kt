package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.inject
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CombFilterAnalyzerTest : BaseTest() {
    private val wavReader by inject<FileReader<Wav>>()
    private val combFilterAnalyzer by inject<CombFilterAnalyzer>()

    @Test
    fun `should detect correct BPM for plain kicks`() {
        val path = "src/test/resources/samples/120bpm_140Hz.wav"
        val wav = wavReader.read(path)
        val result = combFilterAnalyzer.analyze(wav)
        assertEquals(expected = TrackInfo(path, 120.0), actual = result)
    }

    @Test
    fun `should detect correct BPM for refined values`() {
        val path = "src/test/resources/samples/120-5bpm_140Hz.wav"
        val wav = wavReader.read(path)
        val analyzerParams = CombFilterAnalyzerParams(refinementParams = RefinementParams())
        val result = combFilterAnalyzer.analyze(wav, analyzerParams)
        assertEquals(expected = TrackInfo(path, 120.5), actual = result)
    }

    @ParameterizedTest
    @MethodSource("getTracksWithBpm")
    fun `should detect correct BPM for test tracks seconds`(
        trackPath: String,
        bpm: Double,
    ) {
        val wav = wavReader.read(trackPath)
        val result = combFilterAnalyzer.analyze(wav)
        assertNearlyEquals(expected = bpm, actual = result.bpm)
    }
}
