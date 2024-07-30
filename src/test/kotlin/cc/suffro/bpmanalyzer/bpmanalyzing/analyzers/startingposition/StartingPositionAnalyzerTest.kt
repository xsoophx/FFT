package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.inject
import java.util.stream.Stream

class StartingPositionAnalyzerTest : BaseTest() {
    private val wavReader by inject<FileReader<Wav>>()
    private val startingPositionAnalyzer by inject<CacheAnalyzer<Wav, StartingPosition>>()

    @ParameterizedTest
    @MethodSource("getTracksWithBpm")
    fun `should detect correct start position for plain kicks`(
        path: String,
        bpm: Bpm,
        firstSample: Int,
        startInSec: Double,
    ) {
        val wav = wavReader.read(path)
        val result = startingPositionAnalyzer.analyze(wav, StartingPositionCacheAnalyzerParams(bpm))
        assertNearlyEquals(
            expected = StartingPosition(firstSample = firstSample, startInSec = startInSec),
            actual = result,
            sampleDistance = 20,
            secondDistance = 0.01,
        )
    }

    @Test
    fun `should analyze delayed track`() {
        val path = "src/test/resources/tracks/Lucinee, MRD - Bang Juice (MRD Remix).wav"
        val wav = wavReader.read(path)
        val result = startingPositionAnalyzer.analyze(wav, StartingPositionCacheAnalyzerParams(144.0))

        assertNearlyEquals(0.8, result.startInSec, 0.2)
    }

    companion object {
        @JvmStatic
        private fun getTracksWithBpm() =
            Stream.of(
                Arguments.of("src/test/resources/samples/120bpm_140Hz.wav", 120.0, 1174, 0.026621315192743765),
                Arguments.of("src/test/resources/samples/120-5bpm_140Hz.wav", 120.5, 1169, 0.026507936507936508),
                // Arguments.of("src/test/resources/samples/kick_140_24PCM.wav", 17900, 0.40589569160997735),
            )
    }
}
