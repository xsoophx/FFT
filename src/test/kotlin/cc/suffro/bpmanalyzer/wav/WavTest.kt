package cc.suffro.bpmanalyzer.wav

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import kotlin.test.assertEquals

class WavTest : BaseTest() {
    @Test
    fun `equals function should return true if identical wav is being compared`() {
        val wavReader by inject<FileReader<Wav>>()
        val wav1 = wavReader.read(tracksWithBpm.first().path)
        val wav2 = wavReader.read(tracksWithBpm.first().path)

        assertEquals(wav1, wav2)
    }
}
