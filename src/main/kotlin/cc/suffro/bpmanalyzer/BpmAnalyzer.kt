package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.data.Arguments
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import mu.KotlinLogging
import org.koin.core.component.inject
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import java.nio.file.Path

open class BpmAnalyzer : BpmOperations {
    private val logger = KotlinLogging.logger {}

    init {
        KoinManager.INSTANCE
    }

    override fun analyze(args: Array<String>): TrackInfo {
        val arguments = parseArguments(args)
        val result = getFromDbOrAnalyze(arguments)

        println(result)
        return result
    }

    override fun analyze(
        trackPath: String,
        databasePath: String,
    ): TrackInfo = getFromDbOrAnalyze(trackPath, databasePath)

    override fun analyze(
        wav: Wav,
        databasePath: String,
    ): TrackInfo = getFromDbOrAnalyze(wav, databasePath)

    override fun analyze(trackPath: Path): TrackInfo {
        verifyTrackPath(trackPath.toString())
        return analyzeWithoutCache(trackPath.toString())
    }

    override fun analyze(trackPath: String): TrackInfo {
        verifyTrackPath(trackPath)
        return analyzeWithoutCache(trackPath)
    }

    override fun analyze(wav: Wav): TrackInfo {
        verifyTrackPath(wav.filePath.toString())
        return analyzeWithoutCache(wav)
    }

    override fun close() = stopKoin()

    private fun parseArguments(args: Array<String>): Arguments {
        val parser by inject<ArgParser>()

        val trackName by parser.option(ArgType.String, shortName = "t", description = "Path/name of the track")
        val databaseUrl by parser.option(ArgType.String, shortName = "db", description = "Path to the database")
        val checkedDatabaseURL = databaseUrl ?: System.getenv("DATABASE_URL")

        parser.parse(args)
        requireNotNull(trackName) { "Please provide a track name." }
        requireNotNull(checkedDatabaseURL) { "Please provide a database url." }

        return Arguments(trackName!!, checkedDatabaseURL)
    }

    private fun verifyTrackPath(trackPath: String) {
        require(trackPath.isNotEmpty()) { "Please provide a path to your audio file." }
        require(trackPath.endsWith(".wav")) { "Please provide a wav file." }
    }

    private fun verifyDatabasePath(databasePath: String) {
        require(databasePath.isNotEmpty()) { "Please provide a database path." }
    }

    private fun getFromDbOrAnalyze(
        trackPath: String,
        databasePath: String,
    ): TrackInfo {
        val cacheAnalyzer = verifyParametersAndCreateCacheAnalyzer(trackPath, databasePath)
        return cacheAnalyzer.getPathAndAnalyze(trackPath)
    }

    private fun getFromDbOrAnalyze(
        wav: Wav,
        databasePath: String,
    ): TrackInfo {
        val cacheAnalyzer = verifyParametersAndCreateCacheAnalyzer(wav.filePath.toString(), databasePath)
        return cacheAnalyzer.analyze(wav)
    }

    private fun verifyParametersAndCreateCacheAnalyzer(
        filePath: String,
        databasePath: String,
    ): CacheAnalyzer<Wav, TrackInfo> {
        verifyTrackPath(filePath)
        verifyDatabasePath(databasePath)

        val cacheAnalyzer by inject<CacheAnalyzer<Wav, TrackInfo>>(named("ProdImpl")) { parametersOf(databasePath) }
        logger.info { "Using cache analyzer for searching $filePath with DB url $databasePath." }
        return cacheAnalyzer
    }

    private fun getFromDbOrAnalyze(arguments: Arguments): TrackInfo = getFromDbOrAnalyze(arguments.trackPath, arguments.databasePath)

    private fun analyzeWithoutCache(
        trackPath: String,
        showTrackNameOnly: Boolean = false,
    ): TrackInfo {
        val analyzer by inject<Analyzer<Wav, TrackInfo>>()
        val trackInfo = analyzer.getPathAndAnalyze(trackPath)

        return if (showTrackNameOnly) trackInfo.copy(trackName = trackPath.split("/").last()) else trackInfo
    }

    private fun analyzeWithoutCache(
        wav: Wav,
        showTrackNameOnly: Boolean = false,
    ): TrackInfo {
        val analyzer by inject<Analyzer<Wav, TrackInfo>>()
        val trackInfo = analyzer.analyze(wav)

        return if (showTrackNameOnly) trackInfo.copy(trackName = wav.filePath.fileName.toString()) else trackInfo
    }
}
