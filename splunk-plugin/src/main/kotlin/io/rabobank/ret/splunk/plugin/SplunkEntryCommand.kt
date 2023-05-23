package io.rabobank.ret.splunk.plugin

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.RetContext
import io.rabobank.ret.commands.PluginInitializeCommand
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkConfig
import io.rabobank.ret.util.BrowserUtils
import jakarta.ws.rs.core.UriBuilder
import picocli.CommandLine

@TopCommand
@CommandLine.Command(
    name = "splunk",
    description = ["Plugin to interact with Splunk"],
    subcommands = [
        PluginInitializeCommand::class,
    ],
)
@RegisterForReflection(targets = [RetContext::class])
class SplunkEntryCommand(
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
    splunkConfig: SplunkConfig,
) : Runnable {
    @CommandLine.Mixin
    lateinit var contextAwareness: ContextAwareness

    @CommandLine.Option(
        names = ["--index", "-i"],
        description = ["Provide the index to query on"],
        paramLabel = "index",
    )
    var providedIndex: String? = null

    @CommandLine.Option(
        names = ["--app", "-a"],
        description = ["Provide the app name to query on"],
        paramLabel = "appName",
    )
    var providedAppName: String? = null

    @CommandLine.Parameters(
        paramLabel = "query",
        description = ["The Splunk query to execute"],
        arity = "0..*",
    )
    var queryParts: List<String> = emptyList()

    private val splunkUrl = "${splunkConfig.splunkBaseUrl}/en-GB/app/${splunkConfig.splunkApp}/search"

    override fun run() {
        val queryArguments = mutableListOf<String>()

        val appName = providedAppName ?: retContext.gitRepository

        providedIndex?.let { queryArguments += "index=$it" }
        appName?.let { queryArguments += "cf_app_name=$it" }
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

        browserUtils.openUrl(url)
    }
}
