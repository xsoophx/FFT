package cc.suffro.bpmanalyzer.wav

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.FFT
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FftSampleSize
import cc.suffro.bpmanalyzer.wav.data.AudioFormat
import cc.suffro.bpmanalyzer.wav.data.DataChunk
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import cc.suffro.bpmanalyzer.wav.data.Wav
import cc.suffro.bpmanalyzer.wav.data.WindowProcessingParams
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.inject
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.test.assertEquals

@Tag(FFT)
class WAVReaderTest : BaseTest() {
    private val wavReader by inject<FileReader<Wav>>()
    private val fftProcessor by inject<FFTProcessor>()

    @ParameterizedTest
    @MethodSource("getWavDataWithFmt")
    fun `should read correct header and data`(
        path: String,
        fmtChunk: FmtChunk,
        dataChunkSize: Int,
    ) {
        val actual = wavReader.read(path)

        assertEquals(
            expected =
                Wav(
                    filePath = Path(path),
                    fmtChunk = fmtChunk,
                    dataChunk = DataChunk(dataChunkSize, actual.dataChunk.data),
                ),
            actual = actual,
        )
    }

    @Test
    fun `should throw parsing exception`() {
        // TODO: not implemented yet
    }

    @Test
    fun `should read correct Samples if 8-bit PCM`() {
        // TODO: not implemented yet
    }

    @ParameterizedTest
    @MethodSource("getWavDataWithFrequency")
    fun `should read correct Samples from wav file`(
        path: String,
        frequency: Double,
    ) {
        val wav = wavReader.read(path)
        val samples = wav.getWindowContent(channel = 0, begin = 0)
        val fftData = fftProcessor.process(sequenceOf(samples), samplingRate = wav.sampleRate)
        val magnitudes = fftData.first().magnitudes

        assertEquals(
            expected = fftData.first().binIndexOf(frequency),
            actual = magnitudes.indexOf(magnitudes.maxOf { it }),
        )
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 1.0, 0.01, 100",
        "0.0, 4.0, 0.01, 400",
        "1.2, 3.9, 0.15, 18",
    )
    fun `should return correct amount of windows of samples`(
        start: Double,
        end: Double,
        interval: Double,
        expectedWindows: Int,
    ) {
        val wav = wavReader.read("src/test/resources/samples/440.wav")
        val params = WindowProcessingParams(start = start, end = end, interval = interval)
        val actual = wav.getWindows(params)

        assertEquals(expected = expectedWindows, actual = actual.count())
    }

    @Test
    fun `should handle track length as end correctly`() {
        val wav = wavReader.read("src/test/resources/samples/440.wav")
        val windowTime = FftSampleSize.DEFAULT.toDouble() / wav.sampleRate
        val params =
            WindowProcessingParams(start = wav.trackLength - windowTime, end = wav.trackLength, interval = windowTime)
        val actual = wav.getWindows(params)

        assertEquals(expected = 0, actual = actual.count())
    }

    companion object {
        @JvmStatic
        private fun getWavDataWithFmt() =
            Stream.of(
                Arguments.of(
                    "src/test/resources/samples/220.wav",
                    FmtChunk(
                        riffChunkSize = 654006,
                        fmtChunkSize = 16,
                        audioFormat = AudioFormat.PCM,
                        numChannels = 2,
                        sampleRate = 44100,
                        byteRate = 176400,
                        blockAlign = 4,
                        bitsPerSample = 16,
                    ),
                    653868,
                ),
                Arguments.of(
                    "src/test/resources/samples/440.wav",
                    FmtChunk(
                        riffChunkSize = 880110,
                        fmtChunkSize = 16,
                        audioFormat = AudioFormat.PCM,
                        numChannels = 1,
                        sampleRate = 44000,
                        byteRate = 88000,
                        blockAlign = 2,
                        bitsPerSample = 16,
                    ),
                    880000,
                ),
            )

        @JvmStatic
        private fun getWavDataWithFrequency() =
            Stream.of(
                Arguments.of("src/test/resources/samples/440.wav", 440.0),
                Arguments.of("src/test/resources/samples/220.wav", 220.0),
            )
    }
}
