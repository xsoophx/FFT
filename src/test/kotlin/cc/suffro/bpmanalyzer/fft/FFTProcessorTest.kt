package cc.suffro.bpmanalyzer.fft

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.FFT
import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.FftSampleSize
import cc.suffro.bpmanalyzer.fft.data.Method
import cc.suffro.bpmanalyzer.fft.data.Sample
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.fft.data.WindowFunctionType
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.inject
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(FFT)
class FFTProcessorTest : BaseTest() {
    private val fftProcessor by inject<FFTProcessor>()

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft should yield correct results`(
        input: Sequence<Sample>,
        expected: List<Complex>,
    ) {
        val actual = processWindow(input, DEFAULT_SAMPLING_RATE)
        actual.assertNearlyEquals(expected)
    }

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `should return correct values for function with single sequence input`(
        input: Sequence<Sample>,
        expected: List<Complex>,
    ) {
        val actual = fftProcessor.process(input, samplingRate = DEFAULT_SAMPLING_RATE)
        actual.assertNearlyEquals(expected)
    }

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft and dft should return same results`(
        input: Sequence<Sample>,
        expected: List<Complex>,
    ) {
        val fftResults = processWindow(input, samplingRate = DEFAULT_SAMPLING_RATE)
        val dftResults = processWindow(input, DEFAULT_SAMPLING_RATE, Method.R2C_DFT)

        fftResults.assertNearlyEquals(dftResults.output.toList())
        dftResults.assertNearlyEquals(expected)
    }

    // 44100Hz sampling rate, 22050Hz Band, 1024 FFT Size, 512 Bins, df = 44100Hz/1024 = 43.06Hz
    // samples = windowSize
    // Frequency = index * Sampling Frequency / Number of FFT Points
    // Fs = 44100Hz , N = 1024, Ti = 1s, T = Fs/N = df
    @ParameterizedTest
    @ValueSource(ints = [20, 200, 2000])
    fun `should yield correct values for sine signal`(frequency: Int) {
        val signal = getSignalByFrequency(frequency)

        val result = processWindow(signal, samplingRate = DEFAULT_SAMPLING_RATE)

        val magnitudes = result.magnitudes
        val maximumIndex = magnitudes.indexOf(magnitudes.maxOf { it })
        val binIndex = (frequency * FftSampleSize.DEFAULT / DEFAULT_SAMPLING_RATE.toDouble()).roundToInt()

        // TODO: some refactoring, extract into separate tests
        assertEquals(expected = binIndex, actual = maximumIndex)
        assertEquals(expected = binIndex, actual = result.binIndexOf(frequency.toDouble()))
    }

    @ParameterizedTest
    @ValueSource(ints = [43, 430, 4306])
    fun `should yield correct magnitudes for sine signal`(frequency: Int) {
        val amplitude = 2.0.pow(15)
        val signal = getSignalByFrequency(frequency)

        val result = processWindow(signal, samplingRate = DEFAULT_SAMPLING_RATE)
        val magnitudes = result.magnitudes.toList()

        result.binIndexOf(frequency.toDouble()).let {
            assertNearlyEquals(
                // not mathematically correct, but enough for testing purposes
                expected = magnitudes[it],
                actual = amplitude / 2,
                e = 20.0,
                message = "Expected index $it with magnitude ${magnitudes[it]} to be close to ${amplitude / 2}.",
            )
        }
    }

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `should yield correct results for different window functions`(
        input: Sequence<Sample>,
        expected: List<Complex>,
    ) {
        val frequency = 430
        val signal = getSignalByFrequency(frequency)
        val functions = listOf(WindowFunctionType.HAMMING, WindowFunctionType.BLACKMAN, WindowFunctionType.HANNING)

        functions.forEach { function ->
            val actual = processWindow(input = signal, samplingRate = DEFAULT_SAMPLING_RATE, windowFunction = function.function)
            val magnitudes = actual.magnitudes

            val maximumIndex = magnitudes.indexOf(magnitudes.maxOf { it })
            val binIndex = (frequency * FftSampleSize.DEFAULT / DEFAULT_SAMPLING_RATE.toDouble()).roundToInt()

            assertEquals(expected = binIndex, actual = maximumIndex)
            assertEquals(expected = binIndex, actual = actual.binIndexOf(frequency.toDouble()))
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [43, 430, 4306])
    fun `should yield correct values for inverse FFT`(frequency: Int) {
        val signal = getSignalByFrequency(frequency)

        val fftResults = fftProcessor.process(inputSamples = sequenceOf(signal), samplingRate = DEFAULT_SAMPLING_RATE)
        val inverseFftResults = fftProcessor.processInverse(inputSamples = fftResults.map { it.output.asSequence() })
        val firstResult = inverseFftResults.first().toList()

        signal.forEachIndexed { index, value ->
            assertNearlyEquals(
                expected = value,
                actual = firstResult[index],
                message = "Expected index $value to be close to ${firstResult[index]}.",
            )
        }
    }

    private fun processWindow(
        input: Sequence<Double>,
        samplingRate: Int,
        method: Method = Method.FFT_IN_PLACE,
        windowFunction: WindowFunction? = null,
    ): FFTData {
        return fftProcessor.process(
            inputSamples = sequenceOf(input),
            samplingRate = samplingRate,
            method = method,
            windowFunction = windowFunction,
        ).first()
    }

    private fun FFTData.assertNearlyEquals(expected: List<Complex>) {
        output.forEachIndexed { index, complex ->
            assertNearlyEquals(
                expected = complex,
                actual = expected[index],
                message = "Expected ${expected[index]} at index $index, but was $complex.",
            )
        }
    }

    companion object {
        private const val DEFAULT_SAMPLING_RATE = 44100

        @JvmStatic
        fun getFFTValues(): Stream<Arguments> =
            Stream.of(
                Arguments.of(sequenceOf(1.0, 2.0, 3.0, 4.0), listOf(10.R, (-2).R + 2.I, (-2).R, (-2).R + (-2).I)),
                Arguments.of(
                    sequenceOf(0.0, -12539.770711739264, 23170.47500592079, -30273.684521329826),
                    listOf(
                        (-19642.980227).R,
                        (-23170.475006).R - 17733.913810.I,
                        65983.930239.R,
                        (-23170.475006).R + 17733.913810.I,
                    ),
                ),
                Arguments.of(
                    sequenceOf(2.0, 1.0, -1.0, 5.0, 0.0, 3.0, 0.0, -4.0),
                    listOf(
                        6.R,
                        (-5.778175).R - 3.949747.I,
                        3.R - 3.I,
                        9.778175.R - 5.949747.I,
                        (-4).R,
                        9.778175.R + 5.949747.I,
                        3.R + 3.I,
                        (-5.778175).R + 3.949747.I,
                    ),
                ),
            )

        private val cache = mutableMapOf<Int, Sequence<Sample>>()

        fun getSignalByFrequency(
            frequency: Int,
            amplitude: Double = 2.0.pow(15),
        ) = cache.getOrPut(frequency) { signal(frequency.toDouble(), amplitude) }

        private fun signal(
            frequency: Double,
            amplitude: Double,
        ): Sequence<Sample> {
            return (0 until DEFAULT_SAMPLING_RATE).take(FftSampleSize.DEFAULT).asSequence()
                .map { index -> index.toDouble() / DEFAULT_SAMPLING_RATE }
                .map { t -> amplitude * sin(PI * 2.0 * frequency * t) }
        }
    }
}
