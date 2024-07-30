package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.WavWriter
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlinx.cli.ArgParser
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val appModule =
    module {
        single { ArgParser("BPMAnalyzer") }
        single<FileReader<Wav>> { WAVReader }
        single<FileWriter<Wav>> { WavWriter }

        singleOf(::BpmAnalyzer) {
            bind<BpmOperations>()
        }
    }
