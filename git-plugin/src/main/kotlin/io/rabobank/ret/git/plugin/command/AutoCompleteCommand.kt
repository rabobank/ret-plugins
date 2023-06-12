package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.azure.Pipeline
import io.rabobank.ret.git.plugin.azure.PipelineRun
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.config.PluginConfig
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.RegexUtils.DIGITS_PATTERN
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.ScopeType

@Command(name = "autocomplete", hidden = true)
class AutoCompleteCommand(
    private val azureDevopsClient: AzureDevopsClient,
    private val pluginConfig: PluginConfig,
    private val intelliSearch: IntelliSearch,
    private val outputHandler: OutputHandler,
    private val retContext: RetContext,
) {
    companion object {
        private const val TOP_20_PIPELINES = 20
    }

    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "git-pipeline")
    fun printPipelines(@Option(names = ["--word", "-w"]) word: String?) {
        val pipelines = azureDevopsClient.getAllPipelines()

        outputHandler.listPipelines(
            pipelines.value.filter { it.matches(word) }
                .sortedWith(compareBy({ it.folder }, { it.name })),
        )
    }

    @Command(name = "git-pipeline-run")
    fun printPipelineRuns(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(
            required = true,
            names = ["--pipeline-id"],
            description = ["Filter on pipeline"],
        ) pipelineIdFlag: String,
    ) {
        val pipelineId = if (pipelineIdFlag.matches(DIGITS_PATTERN)) {
            pipelineIdFlag
        } else {
            getPipelineByUniqueName(pipelineIdFlag).id.toString()
        }

        val pipelineRuns = azureDevopsClient.getPipelineRuns(pipelineId)

        outputHandler.listPipelineRuns(
            pipelineRuns.value.filter { it.matches(word) }
                .sortedByDescending { it.createdDate }
                .take(TOP_20_PIPELINES),
        )
    }

    @Command(name = "git-repository")
    fun printRepositories(@Option(names = ["--word", "-w"]) word: String?) {
        val repositories = azureDevopsClient.getAllRepositories()

        outputHandler.listRepositories(
            repositories.value.filter {
                word == null || intelliSearch.matches(word, it.name)
            },
        )
    }

    @Command(name = "git-branch")
    fun printBranches(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(names = ["--repository", "-r"]) repositoryFlag: String?,
    ) {
        val repositoryInContext = if (contextAwareness.ignoreContextAwareness) null else retContext.gitRepository
        val repository = if (repositoryFlag.isNullOrBlank()) repositoryInContext else repositoryFlag

        repository?.let { repo ->
            outputHandler.listBranches(
                azureDevopsClient.getAllRefs(repo, "heads/").value
                    .filter { word == null || intelliSearch.matches(word, it.shortName) },
            )
        } ?: outputHandler.error("No repository could be determined")
    }

    @Command(name = "git-pullrequest")
    fun printPullRequests(
        @Option(
            names = ["-n", "--not-reviewed"],
            description = ["Only show Pull Requests that you have not been reviewed"],
        )
        notReviewed: Boolean = false,
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(
            names = ["--repository", "-r"],
            description = ["Filter on repository"],
            scope = ScopeType.INHERIT,
        )
        filterRepository: String? = null,
    ) {
        val prs = azureDevopsClient.getAllPullRequests().value
            .filter { !notReviewed || !it.reviewers.any { r -> r.uniqueName.equals(pluginConfig.email, true) } }
            .filter { it.isFromRepository(filterRepository) }
        val filteredPrs = prs.filter {
            word == null ||
                intelliSearch.matches(word, it.title) ||
                intelliSearch.matches(word, it.repository.name)
        }
        outputHandler.listPRs(filteredPrs)
    }

    private fun PullRequest.isFromRepository(filterRepository: String?): Boolean {
        val repositoryInContext = if (contextAwareness.ignoreContextAwareness) null else retContext.gitRepository
        val filterWord = if (!filterRepository.isNullOrBlank()) filterRepository else repositoryInContext

        return filterWord.isNullOrBlank() || this.repository.name.equals(filterWord, true)
    }

    private fun Pipeline.matches(value: String?): Boolean =
        value == null ||
            intelliSearch.matches(value, name) ||
            intelliSearch.matches(value, cleanedFolder) ||
            intelliSearch.matches(value, uniqueName)

    private fun PipelineRun.matches(word: String?): Boolean =
        word == null || intelliSearch.matches(word, id.toString()) || intelliSearch.matches(word, name) ||
            intelliSearch.matches(word, state.toString()) || intelliSearch.matches(word, result.toString())

    private fun getPipelineByUniqueName(pipelineIdFlag: String) =
        requireNotNull(azureDevopsClient.getAllPipelines().value.firstOrNull { it.uniqueName == pipelineIdFlag }) {
            "Could not find pipeline id by <folder>\\<pipeline-name> combination: '$pipelineIdFlag'"
        }
}
