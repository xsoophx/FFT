package cc.suffro.bpmanalyzer.wav.data

import cc.suffro.bpmanalyzer.fft.data.FftSampleSize
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import java.nio.file.Path
import kotlin.math.abs
import kotlin.math.roundToInt

data class DataChunk(
    val dataChunkSize: Int,
    val data: Array<DoubleArray>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataChunk) return false

        if (dataChunkSize != other.dataChunkSize) return false
        if (!data.contentDeepEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dataChunkSize
        result = 31 * result + data.contentDeepHashCode()
        return result
    }
}

data class FmtChunk(
    val riffChunkSize: Int,
    val fmtChunkSize: Int,
    val audioFormat: AudioFormat,
    val numChannels: Short,
    val sampleRate: Int,
    val byteRate: Int,
    val blockAlign: Short,
    val bitsPerSample: Short,
)

data class Wav(
    val filePath: Path,
    val fmtChunk: FmtChunk,
    val dataChunk: DataChunk,
) {
    /**
     * Takes the header of the original wav file and new data to
     * overwrite the original with the same header information.
     */
    constructor(
        wav: Wav,
        dataChunks: Array<DoubleArray>,
    ) : this(
        wav.filePath,
        wav.fmtChunk,
        DataChunk(dataChunks.first().size * wav.fmtChunk.numChannels * wav.fmtChunk.bitsPerSample / 8, dataChunks),
    )

    fun headerIsEqualTo(other: Wav): Boolean {
        if (this === other) return true

        if (filePath != other.filePath) return false
        if (fmtChunk != other.fmtChunk) return false

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wav) return false

        if (filePath != other.filePath) return false
        if (fmtChunk != other.fmtChunk) return false
        if (dataChunk.dataChunkSize != other.dataChunk.dataChunkSize) return false
        if (!dataChunk.data.contentDeepEquals(other.dataChunk.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + fmtChunk.hashCode()
        result = 31 * result + dataChunk.dataChunkSize.hashCode()
        result = 31 * result + dataChunk.data.contentDeepHashCode()
        return result
    }

    private var defaultChannel = 0
        set(channel) {
            checkChannelRequirements(channel)
            field = channel
        }

    fun defaultChannel() = dataChunk.data[defaultChannel]

    override fun toString(): String = "Filepath:$filePath, fmtChunk:$fmtChunk."

    val sampleRate: Int
        get() = fmtChunk.sampleRate

    val trackLength: Double
        get() = dataChunk.dataChunkSize.toDouble() / (sampleRate * fmtChunk.blockAlign)

    private val indexLastSample: Int
        get() = ((dataChunk.dataChunkSize - fmtChunk.blockAlign) / fmtChunk.blockAlign)

    val timestampLastSample: Double
        get() = indexLastSample.toDouble() / sampleRate

    private fun DoubleArray.get(
        begin: Int,
        length: Int,
    ): Sequence<Double> {
        if (begin < 0 || begin > begin + length || begin + length > this.size) {
            throw IndexOutOfBoundsException("begin $begin, end ${begin + length}, length ${this.size}")
        }
        return this.asSequence().drop(begin).take(length)
    }

    private fun checkChannelRequirements(channel: Int) {
        require(channel >= 0) { "Selected Channel has to be greater than or equal to zero." }
        require(
            channel < dataChunk.dataChunkSize,
        ) { "Selected Channel has to be smaller than available channels (${dataChunk.dataChunkSize})." }
    }

    fun getWindowContent(
        channel: Int,
        begin: Int,
        numSamples: Int = FftSampleSize.DEFAULT,
    ): Sequence<Double> {
        checkChannelRequirements(channel)
        return dataChunk.data[channel].get(begin, numSamples)
    }

    private fun sampleIndexOf(number: Double): Int = (number * sampleRate).roundToInt()

    private fun checkOrCorrectEnd(end: Double): Double = if (end >= trackLength) timestampLastSample else end

    fun getWindows(params: WindowProcessingParams): Sequence<TimeDomainWindow> {
        with(params) {
            checkChannelRequirements(channel)
            val correctedEnd = checkOrCorrectEnd(end)
            return getWindows(sampleIndexOf(start), sampleIndexOf(correctedEnd), interval, channel, numSamples)
        }
    }

    private fun getWindows(
        start: Int,
        end: Int,
        interval: Double,
        channel: Int,
        numSamples: Int,
    ): Sequence<TimeDomainWindow> {
        return getSamples(
            start,
            minOf(end, indexLastSample - numSamples),
            sampleIndexOf(interval),
            channel,
            numSamples,
            interval,
        )
    }

    fun getWindow(
        start: Double = 0.0,
        numSamples: Int,
        channel: Int = 0,
    ): TimeDomainWindow {
        checkChannelRequirements(channel)
        val interval = numSamples.toDouble() / sampleRate
        return getWindow(sampleIndexOf(start), sampleIndexOf(start) + numSamples, interval, channel)
    }

    private fun getWindow(
        start: Int,
        end: Int,
        interval: Double,
        channel: Int,
    ): TimeDomainWindow {
        val numSamples = getHighestPowerOfTwo(end - start)
        val endSample = start + numSamples

        return getSamples(start, endSample, sampleIndexOf(interval), channel, numSamples, interval).first()
    }

    private fun getSamples(
        startSample: Int,
        endSample: Int,
        sampleInterval: Int,
        channel: Int,
        numSamples: Int,
        interval: Double,
    ): Sequence<TimeDomainWindow> {
        val samples =
            (startSample until endSample)
                .step(sampleInterval)
                .asSequence()
                .takeWhile { it < endSample }
                .map { index -> dataChunk.data[channel].get(index, numSamples) }

        return samples.mapIndexed { index, sample -> TimeDomainWindow(sample, interval, index * interval) }
    }

    private fun nearlyEquals(
        epsilonDataSize: Int,
        other: Int,
    ): Boolean {
        return abs(dataChunk.dataChunkSize - other) < epsilonDataSize
    }
}
