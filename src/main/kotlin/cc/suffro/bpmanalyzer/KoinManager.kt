package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.bpmAnalyzingModule
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.filterModule
import cc.suffro.bpmanalyzer.database.databaseModule
import cc.suffro.bpmanalyzer.fft.fftModule
import cc.suffro.bpmanalyzer.speedadjustment.speedAdjusterModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class KoinManager private constructor() {
    init {
        startKoin {
            modules(appModule, databaseModule, speedAdjusterModule, fftModule, bpmAnalyzingModule, filterModule)
        }
    }

    companion object {
        val INSTANCE: KoinManager by lazy { Holder.INSTANCE }
    }

    private object Holder {
        val INSTANCE = KoinManager()
    }

    fun close() {
        stopKoin()
    }
}
