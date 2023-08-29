package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.splunk.plugin.output.OutputHandler
import io.rabobank.ret.splunk.plugin.splunk.SplunkConfig
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "autocomplete", hidden = true)
class AutoCompleteCommand(
    private val splunkConfig: SplunkConfig,
    private val outputHandler: OutputHandler,
    private val intelliSearch: IntelliSearch,
) {
    @Command(name = "indexes")
    fun printIndexes(@Option(names = ["--word", "-w"]) word: String?) {
        outputHandler.listIndexes(
            splunkConfig.indexes
                .filter { word == null || intelliSearch.matches(word, it) },
        )
    }
}
