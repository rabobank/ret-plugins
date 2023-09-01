package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.splunk.plugin.output.OutputHandler
import io.rabobank.ret.splunk.plugin.splunk.SplunkPluginConfig
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "autocomplete", hidden = true)
class AutoCompleteCommand(
    private val splunkConfig: SplunkPluginConfig,
    private val outputHandler: OutputHandler,
    private val intelliSearch: IntelliSearch,
) {
    @Command(name = "indexes")
    fun printIndexes(@Option(names = ["--word", "-w"]) word: String?) {
        outputHandler.listIndexes(
            splunkConfig.config.indexes
                .filter { word == null || intelliSearch.matches(word, it) },
        )
    }

    @Command(name = "projects")
    fun printProjects(
        @Option(names = ["--word", "-w"]) word: String?,
    ) {
        outputHandler.listProjects(
            splunkConfig.config.projects
                .map { it.name }
                .filter { word == null || intelliSearch.matches(word, it) },
        )
    }
}
