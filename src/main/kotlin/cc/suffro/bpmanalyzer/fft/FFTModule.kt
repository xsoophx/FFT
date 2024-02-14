package cc.suffro.bpmanalyzer.fft

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val fftModule = module {
    singleOf(::FFTProcessor)
}
