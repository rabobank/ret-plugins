package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.output.OutputHandler
import io.rabobank.ret.splunk.plugin.splunk.SplunkConfig
import io.rabobank.ret.splunk.plugin.splunk.SplunkData
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option

@Command(name = "autocomplete", hidden = true)
class AutoCompleteCommand(
    private val splunkData: SplunkData,
    private val splunkConfig: SplunkConfig,
    private val intelliSearch: IntelliSearch,
    private val outputHandler: OutputHandler,
) {

    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "splunk-app-name")
    fun printSplunkCfAppName(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(names = ["--index", "-i"]) indexFlag: String?,
    ) {
        outputHandler.listSplunkAppNames(
            splunkData.getAll()
                .asSequence()
                .filter { splunkConfig.cfSpaceNames.isEmpty() || splunkConfig.cfSpaceNames.contains(it.cfSpaceName) }
                .filter { indexFlag.isNullOrBlank() || it.index == indexFlag }
                .distinctBy { it.cfAppName }
                .filter { word == null || intelliSearch.matches(word, it.cfAppName) }
                .sortedBy { it.cfAppName }
                .toList(),
        )
    }

    @Command(name = "splunk-index")
    fun printSplunkIndex(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(names = ["--app", "-a"]) appFlag: String?,
    ) {
        outputHandler.listSplunkIndexes(
            splunkData.getAll()
                .asSequence()
                .filter { splunkConfig.cfSpaceNames.isEmpty() || splunkConfig.cfSpaceNames.contains(it.cfSpaceName) }
                .filter { appFlag.isNullOrBlank() || it.cfAppName == appFlag }
                .filter { word.isNullOrBlank() || intelliSearch.matches(word, it.index) }
                .distinctBy { it.index }
                .sortedBy { it.index }
                .toList(),
        )
    }
}
