package cc.suffro.bpmanalyzer.ui

import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import java.util.LinkedList

internal data class SampleCollector(
    val samples: LinkedList<FFTData>,
) : List<FFTData> by samples

internal class SampleCreator(val fftProcessor: FFTProcessor)
