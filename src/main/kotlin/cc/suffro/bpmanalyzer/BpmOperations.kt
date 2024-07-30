package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.component.KoinComponent
import java.io.Closeable
import java.nio.file.Path

interface BpmOperations : Closeable, KoinComponent {
    fun analyze(args: Array<String>): TrackInfo

    fun analyze(
        trackPath: String,
        databasePath: String,
    ): TrackInfo

    fun analyze(
        wav: Wav,
        databasePath: String,
    ): TrackInfo

    fun analyze(
        trackPath: Path,
        databasePath: Path,
    ): TrackInfo {
        return analyze(trackPath.toString(), databasePath.toString())
    }

    fun analyze(
        trackPath: String,
        databasePath: Path,
    ): TrackInfo {
        return analyze(trackPath, databasePath.toString())
    }

    fun analyze(
        trackPath: Path,
        databasePath: String,
    ): TrackInfo {
        return analyze(trackPath.toString(), databasePath)
    }

    fun analyze(trackPath: Path): TrackInfo

    fun analyze(trackPath: String): TrackInfo

    fun analyze(wav: Wav): TrackInfo
}
