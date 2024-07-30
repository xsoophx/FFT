package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPositionCacheAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.speedadjustment.SpeedAdjuster
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.Wav
import mu.KotlinLogging
import java.io.File
import java.nio.file.Path

internal class TrackMerger(
    private val cacheAnalyzer: CacheAnalyzer<Wav, TrackInfo>,
    private val speedAdjuster: SpeedAdjuster,
    private val startingPositionAnalyzer: CacheAnalyzer<Wav, StartingPosition>,
    private val wavWriter: FileWriter<Wav>,
    private val wavReader: FileReader<Wav>,
) {
    fun merge(
        trackOne: Wav,
        trackTwo: Wav,
        targetBpm: Double,
    ): Wav {
        val bpmOne = cacheAnalyzer.analyze(trackOne).bpm
        val bpmTwo = cacheAnalyzer.analyze(trackTwo).bpm
        return mergeWavs(trackOne to bpmOne, trackTwo to bpmTwo, targetBpm)
    }

    fun <T> merge(
        trackOne: Pair<T, Bpm>,
        trackTwo: Pair<T, Bpm>,
        targetBpm: Double,
    ): Wav {
        when (trackOne.first) {
            is Wav -> {
                val trackOneWav = trackOne.first as Wav
                val trackTwoWav = trackTwo.first as Wav
                return mergeWavs(trackOneWav to trackOne.second, trackTwoWav to trackTwo.second, targetBpm)
            }

            is String -> {
                val trackOneWav = wavReader.read(trackOne.first as String)
                val trackTwoWav = wavReader.read(trackTwo.first as String)
                return mergeWavs(trackOneWav to trackOne.second, trackTwoWav to trackTwo.second, targetBpm)
            }

            else -> {
                throw IllegalArgumentException("Only Wav files and Strings are supported")
            }
        }
    }

    private fun mergeWavs(
        trackOne: Pair<Wav, Bpm>,
        trackTwo: Pair<Wav, Bpm>,
        targetBpm: Double,
    ): Wav {
        val stretchedTrackOne = Wav(trackOne.first, speedAdjuster.changeTo(trackOne.first, trackOne.second, targetBpm))
        val stretchedTrackTwo = Wav(trackTwo.first, speedAdjuster.changeTo(trackTwo.first, trackOne.second, targetBpm))

        val maxLength = maxOf(stretchedTrackOne.dataChunk.data[0].size, stretchedTrackTwo.dataChunk.data[0].size)
        val numberOfChannels = maxOf(stretchedTrackOne.dataChunk.data.size, stretchedTrackTwo.dataChunk.data.size)
        val mergedSamples = Array(numberOfChannels) { DoubleArray(maxLength) { 0.0 } }

        val startingPositionOne =
            startingPositionAnalyzer.analyze(trackOne.first, StartingPositionCacheAnalyzerParams(trackOne.second))
        val startingPositionTwo =
            startingPositionAnalyzer.analyze(trackTwo.first, StartingPositionCacheAnalyzerParams(trackTwo.second))

        stretchedTrackOne.stretchAndWrite(startingPositionOne, maxLength, mergedSamples)
        stretchedTrackTwo.stretchAndWrite(startingPositionTwo, maxLength, mergedSamples)

        val filePath = Path.of(getMergedPathByPaths(trackOne.first.filePath, trackTwo.first.filePath))

        val mergedWav =
            Wav(trackOne.first.copy(filePath = filePath), dataChunks = mergedSamples)
        wavWriter.write(filePath, mergedWav)

        return mergedWav
    }

    fun merge(
        directoryPath: String,
        targetBpm: Double,
    ) {
        val wavTracks = getWavTracksOfDirectory(directoryPath)
        merge(wavTracks, targetBpm)
    }

    // TODO: improve interruptibility for future use
    fun merge(
        tracks: List<Wav>,
        targetBpm: Double,
    ) {
        tracks.forEach { trackA ->
            val trackNameA = trackA.filePath.nameWithoutExtension()
            tracks.forEach { trackB ->
                if (trackA != trackB) {
                    val trackNameB = trackB.filePath.nameWithoutExtension()
                    merge(trackA, trackB, targetBpm)
                    logger.info("Merged $trackNameA and $trackNameB")
                }
            }
        }
    }

    fun getMergedPathByPaths(
        pathA: String,
        pathB: String,
    ): String {
        val trackNameA = pathA.nameWithoutExtension()
        val trackNameB = pathB.nameWithoutExtension()
        return "${COPY_BASE_PATH}${trackNameA}_$trackNameB.wav"
    }

    fun getMergedPathByPaths(
        pathA: Path,
        pathB: Path,
    ): String {
        return getMergedPathByNames(pathA.nameWithoutExtension(), pathB.nameWithoutExtension())
    }

    private fun getMergedPathByNames(
        nameA: String,
        nameB: String,
    ): String {
        return "${COPY_BASE_PATH}${nameA}_$nameB.wav"
    }

    private fun Path.nameWithoutExtension(): String {
        val file = this.toFile()
        return file.nameWithoutExtension
    }

    private fun String.nameWithoutExtension(): String {
        return Path.of(this).nameWithoutExtension()
    }

    private fun getWavTracksOfDirectory(directoryPath: String): List<Wav> {
        val directory = File(directoryPath)
        require(directory.exists() && directory.isDirectory) {
            throw IllegalArgumentException("Directory with path $directoryPath does not exist or is not a directory.")
        }

        val wavPaths =
            directory.walk()
                .filter { it.isFile && it.extension.equals("wav", ignoreCase = true) }
                .map { it.absolutePath }
                .toList()

        return wavPaths.map(wavReader::read)
    }

    private fun Wav.stretchAndWrite(
        startingPosition: StartingPosition,
        maxLength: Int,
        mergedSamples: Array<DoubleArray>,
    ) {
        for (channel in dataChunk.data.indices) {
            for (i in dataChunk.data[channel].indices) {
                val position = i + startingPosition.firstSample
                if (position < maxLength) {
                    mergedSamples[channel][position] += dataChunk.data[channel][i] / 2
                }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }

        const val COPY_BASE_PATH = "src/test/resources/copies/"
    }
}
