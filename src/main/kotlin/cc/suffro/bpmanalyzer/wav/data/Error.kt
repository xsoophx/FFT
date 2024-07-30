package cc.suffro.bpmanalyzer.wav.data

internal enum class ErrorType {
    NOT_A_RIFF,
    NOT_A_WAV,
    UNEXPECTED_DATA_SIGNATURE,
    UNEXPECTED_EOF,
    UNEXPECTED_FMT_SIGNATURE,
    WRONG_DATA_SIZE,
    ;

    override fun toString() =
        when (this) {
            NOT_A_RIFF -> "No RIFF header detected."
            NOT_A_WAV -> "RIFF is not of type WAV."
            UNEXPECTED_DATA_SIGNATURE -> "File contains invalid data signature."
            UNEXPECTED_EOF -> "RIFF file ended unexpectedly."
            UNEXPECTED_FMT_SIGNATURE -> "File contains invalid fmt signature."
            WRONG_DATA_SIZE -> "Data Chunk doesn't have the required size."
        }
}

internal data class Error(
    val type: ErrorType,
    var message: String = "",
) {
    init {
        message = type.toString()
    }
}
