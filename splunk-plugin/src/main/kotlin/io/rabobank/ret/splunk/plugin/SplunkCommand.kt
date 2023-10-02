package io.rabobank.ret.splunk.plugin

import io.quarkus.logging.Log
import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.RetContext
import io.rabobank.ret.commands.PluginConfigureCommand
import io.rabobank.ret.commands.PluginInitializeCommand
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkPluginConfig
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.Logged
import jakarta.ws.rs.core.UriBuilder
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@TopCommand
@Command(
    name = "splunk",
    description = ["Plugin to interact with Splunk"],
    subcommands = [
        PluginInitializeCommand::class,
        PluginConfigureCommand::class,
        AutoCompleteCommand::class,
    ],
)
@RegisterForReflection(targets = [RetContext::class])
@Logged
class SplunkCommand(
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
    private val splunkConfig: SplunkPluginConfig,
) : Runnable {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Option(
        names = ["--index", "-i"],
        description = ["Provide the index to query on"],
        paramLabel = "index",
        completionCandidates = IndexCompletionCandidates::class,
    )
    var providedIndex: String? = null

    @Option(
        names = ["--project", "-p"],
        description = ["Provide the project name to query on"],
        paramLabel = "projectName",
        completionCandidates = ProjectCompletionCandidates::class,
    )
    var providedProjectName: String? = null

    @Parameters(
        paramLabel = "query",
        description = ["The Splunk query to execute"],
        arity = "0..*",
    )
    var queryParts: List<String> = emptyList()

    private val splunkUrl by lazy { "${splunkConfig.config.baseUrl}/en-US/app/${splunkConfig.config.app}/search" }

    override fun run() {
        val queryArguments = mutableListOf<String?>()

        val projectName = providedProjectName ?: retContext.gitRepository?.removeSuffix(".git")
        val searchField = splunkConfig.config.searchField ?: "project"

        queryArguments += providedIndex?.let { "index=$it" } ?: splunkConfig.config.indexes.joinToStringOrSingle()
        projectName?.let { queryArguments += "$searchField=$it" }
        queryArguments += queryParts

        val query = queryArguments.filterNotNull().joinToString(" ")

        val url =
            UriBuilder.fromUri(splunkUrl)
                .apply {
                    if (query.isNotBlank()) {
                        queryParam("q", "search $query")
                    }
                }
                .build()
                .toASCIIString()

        Log.info("Querying splunk with url '$url'")
        browserUtils.openUrl(url)
    }

    private fun List<String>.joinToStringOrSingle() =
        takeIf { it.size > 1 }?.joinToString(separator = " OR ", prefix = "(", postfix = ")") { "index=$it" }
            ?: firstOrNull()
}

internal class IndexCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_splunk_index").iterator()
}

internal class ProjectCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_splunk_project").iterator()
}
