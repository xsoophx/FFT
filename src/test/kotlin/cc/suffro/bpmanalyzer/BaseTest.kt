package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.analyzerTestModule
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.filterTestModule
import cc.suffro.bpmanalyzer.database.databaseTestModule
import cc.suffro.bpmanalyzer.fft.fftTestModule
import cc.suffro.bpmanalyzer.speedadjustment.speedAdjusterTestModule
import cc.suffro.bpmanalyzer.trackmerge.trackMergerTestModule
import cc.suffro.bpmanalyzer.wav.wavTestModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import kotlin.streams.asStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseTest : KoinTest {
    companion object {
        private val predefinedSamples =
            mapOf(
                "src/test/resources/samples/120bpm_140Hz.wav" to 120.0,
                "src/test/resources/samples/120-5bpm_140Hz.wav" to 120.5,
                "src/test/resources/samples/kick_140_16PCM.wav" to 140.0,
                // add these after implementing correct 24 and 32 bit support
                // "src/test/resources/samples/kick_140_24PCM.wav" to 140.0,
                // "src/test/resources/samples/kick_140_32PCM.wav" to 140.0,
            ).map { BaseTestTrackInfo(it.key, it.value) }

        // custom tracks can be defined here
        // for using custom tracks, set the environment variable USE_CUSTOM_TRACKS to true
        private val customTracks =
            mapOf(
                "src/test/resources/tracks/HXIST - Tier.wav" to 147.0,
                "src/test/resources/tracks/Jan Vercauteren - Dysfunction.wav" to 149.0,
                "src/test/resources/tracks/Lucinee, MRD - Bang Juice (MRD Remix).wav" to 144.0,
                "src/test/resources/tracks/Mark Terre - Gravity Zero.wav" to 152.0,
                "src/test/resources/tracks/Peter Van Hoesen - Vertical Vertigo.wav" to 135.0,
            ).map { BaseTestTrackInfo(it.key, it.value) }

        val tracksWithBpm = if (System.getenv("USE_CUSTOM_TRACKS") == "true") customTracks else predefinedSamples

        @JvmStatic
        fun getTracksWithBpm() = tracksWithBpm.asSequence().map { Arguments.of(it.path, it.bpm) }.asStream()

        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                printLogger(Level.DEBUG)
                modules(
                    databaseTestModule,
                    speedAdjusterTestModule,
                    wavTestModule,
                    fftTestModule,
                    analyzerTestModule,
                    trackMergerTestModule,
                    filterTestModule,
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }
}

data class BaseTestTrackInfo(val path: String, val bpm: Double)
