package cc.suffro.bpmanalyzer

internal class Main : BpmAnalyzer() {
    fun run(args: Array<String>) {
        use { analyze(args) }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main().run(args)
        }
    }
}
