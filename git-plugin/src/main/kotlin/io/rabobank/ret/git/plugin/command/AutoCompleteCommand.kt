package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.Logged
import io.rabobank.ret.util.RegexUtils.DIGITS_PATTERN
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.ScopeType

@Command(name = "autocomplete", hidden = true)
@Logged
class AutoCompleteCommand(
    private val gitProvider: GitProvider,
    private val intelliSearch: IntelliSearch,
    private val outputHandler: OutputHandler,
    private val retContext: RetContext,
) {
    companion object {
        private const val TOP_20_PIPELINES = 20
    }

    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "pipeline")
    fun printPipelines(@Option(names = ["--word", "-w"]) word: String?) {
        val pipelines = gitProvider.getAllPipelines()

        outputHandler.listPipelines(
            pipelines.filter { it.matches(word) }
                .sortedWith(compareBy({ it.container }, { it.name })),
        )
    }

    @Command(name = "pipeline-run")
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

        val pipelineRuns = gitProvider.getPipelineRuns(pipelineId)

        outputHandler.listPipelineRuns(
            pipelineRuns.filter { it.matches(word) }
                .sortedByDescending { it.createdDate }
                .take(TOP_20_PIPELINES),
        )
    }

    @Command(name = "repository")
    fun printRepositories(@Option(names = ["--word", "-w"]) word: String?) {
        val repositories = gitProvider.getAllRepositories()

        outputHandler.listRepositories(
            repositories.filter {
                word == null || intelliSearch.matches(word, it.name)
            },
        )
    }

    @Command(name = "branch")
    fun printBranches(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(names = ["--repository", "-r"]) repositoryFlag: String?,
    ) {
        val repositoryInContext = if (contextAwareness.ignoreContextAwareness) null else retContext.gitRepository
        val repository = if (repositoryFlag.isNullOrBlank()) repositoryInContext else repositoryFlag

        repository?.let { repo ->
            outputHandler.listBranches(
                gitProvider.getAllRefs(repo, "heads/")
                    .filter { word == null || intelliSearch.matches(word, it.shortName) },
            )
        } ?: outputHandler.error("No repository could be determined")
    }

    @Command(name = "pullrequest")
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
        val prs = if (!notReviewed) gitProvider.getAllPullRequests() else gitProvider.getPullRequestsNotReviewedByUser()
        val filteredPrs = prs
            .filter { it.isFromRepository(filterRepository) }
            .filter {
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

    private fun Pipeline.matches(value: String?) =
        value == null ||
            intelliSearch.matches(value, name) ||
            intelliSearch.matches(value, container) ||
            intelliSearch.matches(value, uniqueName)

    private fun PipelineRun.matches(word: String?) =
        word == null || intelliSearch.matches(word, id.toString()) || intelliSearch.matches(word, name) ||
            intelliSearch.matches(word, state.toString()) || intelliSearch.matches(word, result.toString())

    private fun getPipelineByUniqueName(pipelineIdFlag: String) =
        requireNotNull(gitProvider.getAllPipelines().firstOrNull { it.uniqueName == pipelineIdFlag }) {
            "Could not find pipeline id by <folder>\\<pipeline-name> combination: '$pipelineIdFlag'"
        }
}
