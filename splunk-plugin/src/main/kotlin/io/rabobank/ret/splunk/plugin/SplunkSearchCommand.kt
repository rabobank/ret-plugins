package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.RetContext
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkConfig
import io.rabobank.ret.util.BrowserUtils
import jakarta.ws.rs.core.UriBuilder
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "search", description = ["Open Splunk search results"])
internal class SplunkSearchCommand(
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
    splunkConfig: SplunkConfig,
) : Runnable {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Option(
        names = ["--index", "-i"],
        description = ["Provide the index to query on"],
        paramLabel = "index",
        completionCandidates = SplunkIndexCompletionCandidates::class,
    )
    var providedIndex: String? = null

    @Option(
        names = ["--app", "-a"],
        description = ["Provide the app name to query on"],
        paramLabel = "appName",
        completionCandidates = SplunkAppCompletionCandidates::class,
    )
    var providedAppName: String? = null

    @Parameters
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

internal class SplunkAppCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_splunk_app").iterator()
}

internal class SplunkIndexCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_splunk_index").iterator()
}
