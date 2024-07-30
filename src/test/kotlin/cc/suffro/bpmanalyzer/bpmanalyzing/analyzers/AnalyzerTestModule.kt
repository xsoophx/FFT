package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzerTestImpl
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPositionAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val analyzerTestModule =
    module {
        singleOf(::CombFilterAnalyzer) {
            bind<Analyzer<Wav, TrackInfo>>()
        }

        single { CombFilterCacheAnalyzerTestImpl() }
        single<CacheAnalyzer<Wav, TrackInfo>>(named("TestImpl")) { get<CombFilterCacheAnalyzerTestImpl>() }
        single<Analyzer<Wav, TrackInfo>>(named("TestImpl")) { get<CombFilterCacheAnalyzerTestImpl>() }

        singleOf(::StartingPositionAnalyzer) {
            bind<CacheAnalyzer<Wav, StartingPosition>>()
        }
        singleOf(::CombFilterCacheAnalyzer) {
            bind<CacheAnalyzer<Wav, TrackInfo>>()
            named("ProdImpl")
        }

        singleOf(::StartingPositionAnalyzer)
    }
