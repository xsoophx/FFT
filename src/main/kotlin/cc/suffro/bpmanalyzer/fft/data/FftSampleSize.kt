package cc.suffro.bpmanalyzer.fft.data

@JvmInline
value class FftSampleSize(val size: Int) {
    init {
        require((size != 0) && size and (size - 1) == 0) { "Length has to be power of two, but is $size." }
    }

    companion object {
        const val TWO_HUNDRED = 256
        const val FIVE_HUNDRED = 512
        const val DEFAULT = 1024
        const val TWO_THOUSAND = 2048
        const val FOUR_THOUSAND = 4096
    }
}
