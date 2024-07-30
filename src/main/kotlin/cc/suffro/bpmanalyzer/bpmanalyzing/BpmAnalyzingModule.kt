package cc.suffro.bpmanalyzer.bpmanalyzing

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPositionAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val bpmAnalyzingModule =
    module {

        single<CacheAnalyzer<Wav, TrackInfo>>(named("ProdImpl")) { get<CombFilterCacheAnalyzer>() }
        single<Analyzer<Wav, TrackInfo>>(named("ProdImpl")) { get<CombFilterCacheAnalyzer>() }

        singleOf(::CombFilterAnalyzer) {
            bind<Analyzer<Wav, TrackInfo>>()
        }
        singleOf(::StartingPositionAnalyzer) {
            bind<CacheAnalyzer<Wav, StartingPosition>>()
        }
    }
