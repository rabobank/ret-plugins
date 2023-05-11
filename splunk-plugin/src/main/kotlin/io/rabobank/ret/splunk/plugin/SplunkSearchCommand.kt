package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.RetContext
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkData
import io.rabobank.ret.util.BrowserUtils
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "search", description = ["Open Splunk search results"])
internal class SplunkSearchCommand(
    private val browserUtils: BrowserUtils,
    private val splunkData: SplunkData,
    private val retContext: RetContext,
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

    @ConfigProperty(name = "splunk.baseUrl")
    lateinit var splunkBaseUrl: String

    override fun run() {
        val queryArguments = mutableListOf<String>()

        val appName = providedAppName ?: retContext.gitRepository
        val index = determineIndex(appName)

        index?.let { queryArguments += "index=$it" }
        appName?.let { queryArguments += "cf_app_name=$it" }
        queryArguments += queryParts

        val query = queryArguments.joinToString(" ")

        val url = UriBuilder.fromUri(splunkBaseUrl)
            .apply {
                if (query.isNotBlank()) {
                    queryParam("q", "search $query")
                }
            }
            .build()
            .toASCIIString()

        browserUtils.openUrl(url)
    }

    private fun determineIndex(appName: String?): String? {
        return when {
            providedIndex != null || appName == null -> providedIndex
            else -> splunkData.getAllByAppName(appName)
                .map { it.index }
                .sortedWith(this::compareByProdIndex)
                .firstOrNull()
        }
    }

    private fun compareByProdIndex(indexA: String, indexB: String): Int {
        val isIndexAProd = indexA.endsWith("_p")
        val isIndexBProd = indexB.endsWith("_p")

        return when {
            isIndexAProd && !isIndexBProd -> return -1
            isIndexBProd && !isIndexAProd -> return 1
            else -> indexA.compareTo(indexB) // Alphabetical order
        }
    }
}

internal class SplunkAppCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_splunk_app").iterator()
}

internal class SplunkIndexCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_splunk_index").iterator()
}
