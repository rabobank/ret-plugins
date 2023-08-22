package io.rabobank.ret.splunk.plugin

import io.quarkus.logging.Log
import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.RetContext
import io.rabobank.ret.commands.PluginConfigureCommand
import io.rabobank.ret.commands.PluginInitializeCommand
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkConfig
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
    ],
)
@RegisterForReflection(targets = [RetContext::class])
@Logged
class SplunkEntryCommand(
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
    private val splunkConfig: SplunkConfig,
) : Runnable {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Option(
        names = ["--index", "-i"],
        description = ["Provide the index to query on"],
        paramLabel = "index",
    )
    var providedIndex: String? = null

    @Option(
        names = ["--app", "-a"],
        description = ["Provide the app name to query on"],
        paramLabel = "appName",
    )
    var providedAppName: String? = null

    @Parameters(
        paramLabel = "query",
        description = ["The Splunk query to execute"],
        arity = "0..*",
    )
    var queryParts: List<String> = emptyList()

    private val splunkUrl = "${splunkConfig.baseUrl}/en-US/app/${splunkConfig.app}/search"

    override fun run() {
        val queryArguments = mutableListOf<String>()

        val appName = providedAppName ?: retContext.gitRepository?.removeSuffix(".git")
        val searchField = splunkConfig.searchField ?: "appName"

        queryArguments += providedIndex?.let { "index=$it" }
            ?: splunkConfig.indexes.joinToString(" OR ", "(", ")") { "index=$it" }
        appName?.let { queryArguments += "$searchField=$it" }
        queryArguments += queryParts

        val query = queryArguments.joinToString(" ")

        val url = UriBuilder.fromUri(splunkUrl)
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
}
