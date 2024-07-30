package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo
import java.nio.file.Path

interface DatabaseOperations {
    fun saveTrackInfo(
        trackName: String,
        bpm: Double,
    ): Int

    fun saveTrackInfo(trackInfo: TrackInfo): Int = saveTrackInfo(trackInfo.trackName, trackInfo.bpm)

    fun getTrackInfo(trackName: String): TrackInfo?

    fun getTrackInfo(trackName: Path): TrackInfo?

    fun cleanUpDatabase(): Boolean
}
