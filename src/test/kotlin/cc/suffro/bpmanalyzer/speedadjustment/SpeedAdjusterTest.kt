package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.wav.data.AudioFormat
import cc.suffro.bpmanalyzer.wav.data.DataChunk
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.qualifier.named
import org.koin.test.inject
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpeedAdjusterTest : BaseTest() {
    private val prodSpeedAdjuster by inject<SpeedAdjuster>(named("ProdSpeedAdjuster"))

    private val testSpeedAdjuster by inject<SpeedAdjuster>(named("TestSpeedAdjuster"))

    private val wavReader by inject<FileReader<Wav>>()

    private val wavWriter by inject<FileWriter<Wav>>()

    @Test
    fun `should create correct number of samples`() {
        val data = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)
        val wav =
            Wav(
                filePath = Path.of("dummy.wav"),
                fmtChunk = FmtChunk(1, 1, AudioFormat.PCM, 1, 1, 1, 1, 1),
                dataChunk = DataChunk(0, arrayOf(data)),
            )

        val result = testSpeedAdjuster.changeTo(wav, 120.0)
        assertEquals(10, result.first().size)
    }

    @Test
    fun `should create correct number of samples for bigger number`() {
        val content = DoubleArray(1000) { it.toDouble() }
        val data = arrayOf(content, content)

        val wav =
            Wav(
                filePath = Path.of("dummy.wav"),
                fmtChunk = FmtChunk(1, 1, AudioFormat.PCM, 2, 1, 1, 1, 1),
                dataChunk = DataChunk(0, data),
            )

        val result = testSpeedAdjuster.changeTo(wav, 120.0)
        assertEquals((content.size * (100.0 / 120.0)).toInt(), result.first().size)
    }

    @ParameterizedTest
    @MethodSource("getTracksWithBpm")
    fun `should create correct pitched track`(
        path: String,
        currentBpm: Double,
    ) {
        val wav = wavReader.read(path)
        val targetBpm = 100.0
        val result = prodSpeedAdjuster.changeTo(wav, targetBpm)
        val bytesPerSample = wav.fmtChunk.bitsPerSample / 8.0

        assertNearlyEquals(
            expected =
                (wav.dataChunk.dataChunkSize / ((bytesPerSample) * wav.fmtChunk.numChannels)) * (currentBpm / targetBpm),
            actual = result.first().size.toDouble(),
            e = 2,
            exclusive = false,
        )
    }

    @Test
    fun `should create correct pitched track for Double value`() {
        val wav = wavReader.read("src/test/resources/samples/120bpm_140Hz.wav")
        val targetBpm = 120.5
        val currentBpm = 120.0
        val result = prodSpeedAdjuster.changeTo(wav, targetBpm)

        assertEquals(
            expected = (wav.dataChunk.dataChunkSize * (currentBpm / targetBpm)).toInt(),
            actual = result.first().size * wav.fmtChunk.numChannels * wav.fmtChunk.bitsPerSample / 8,
        )

        wavWriter.write(
            Path.of("src/test/resources/samples/120-5bpm_140Hz.wav"),
            Wav(wav, result),
        )
    }

    @Test
    fun `should create correct wav file with custom path`() {
        val wav = wavReader.read("src/test/resources/samples/120bpm_140Hz.wav")
        val currentBpm = 120.0
        val targetBpm = 130.0
        val customPath = Path.of("src/test/resources/samples/130bpm_140Hz.wav")
        val result = prodSpeedAdjuster.changeWavTo(wav, targetBpm, customPath)

        assertNearlyEquals(
            expected = (wav.dataChunk.dataChunkSize * (currentBpm / targetBpm)).toInt(),
            actual = result.dataChunk.data.first().size * wav.fmtChunk.numChannels * wav.fmtChunk.bitsPerSample / 8,
            e = 3,
            exclusive = false,
        )
        assertEquals(customPath, result.filePath)
        assertTrue(wav.copy(filePath = customPath).headerIsEqualTo(result))
    }
}
