package cc.suffro.bpmanalyzer.trackmerge

import cc.suffro.bpmanalyzer.TrackMerger
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.qualifier.named
import org.koin.dsl.module

val trackMergerTestModule =
    module {
        single<CacheAnalyzer<Wav, TrackInfo>>(named("ProdImpl")) { get<CombFilterCacheAnalyzer>() }
        single<Analyzer<Wav, TrackInfo>>(named("ProdImpl")) { get<CombFilterCacheAnalyzer>() }

        factory {
            TrackMerger(
                get(named("ProdImpl")),
                get(named("ProdSpeedAdjuster")),
                get(),
                get(),
                get(),
            )
        }
    }
