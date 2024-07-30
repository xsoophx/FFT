package cc.suffro.bpmanalyzer.trackmerge

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.TrackMerger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
import org.koin.test.inject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

// TODO: check Koin dependency
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TrackMergerTest : BaseTest() {
    private val trackMerger by inject<TrackMerger>()

    private val first = tracksWithBpm.first()
    private val second = tracksWithBpm.first()
    private val path = trackMerger.getMergedPathByPaths(first.path, second.path)

    @AfterEach
    fun cleanUp() {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun `should merge two tracks correctly`() {
        val targetBpm = 140.0
        assertTrue(!File(path).exists())

        trackMerger.merge(first.path to first.bpm, second.path to second.bpm, targetBpm)
        assertTrue(File(path).exists())
    }

    @Test
    @Disabled
    fun `should merge two tracks correctly with different target bpm`() {
        val targetBpm = 130.0
        val trackOne = "src/test/resources/tracks/Lucinee, MRD - Bang Juice (MRD Remix).wav"
        val trackTwo = "src/test/resources/samples/120bpm_140Hz.wav"

        trackMerger.merge(trackOne to 144.0, trackTwo to 120.0, targetBpm)
    }
}
