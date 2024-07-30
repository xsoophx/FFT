package cc.suffro.bpmanalyzer.fft

import org.koin.dsl.module

internal val fftModule =
    module {
        single { FFTProcessor }
    }
