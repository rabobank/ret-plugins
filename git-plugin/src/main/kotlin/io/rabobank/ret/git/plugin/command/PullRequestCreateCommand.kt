package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.jboss.resteasy.reactive.RestResponse.StatusCode.CONFLICT
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ScopeType

@Command(
    name = "create",
    description = ["Create a pull request"],
)
class PullRequestCreateCommand(
    private val gitProvider: GitProvider,
    private val browserUtils: BrowserUtils,
    private val outputHandler: OutputHandler,
    private val retContext: RetContext,
) : Runnable {

    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Parameters(
        arity = "0..1",
        description = ["Branch name to create pull request for"],
        paramLabel = "<branch>",
        completionCandidates = BranchCompletionCandidates::class,
    )
    var providedBranch: String? = null

    @Option(
        names = ["--no-prompt"],
        description = ["Create the PR directly, instead of navigating to the prompt"],
        required = false,
        defaultValue = "false",
    )
    var noPrompt: Boolean = false

    @Option(
        names = ["--repository", "-r"],
        description = ["Filter on repository"],
        scope = ScopeType.INHERIT,
        completionCandidates = RepositoryFlagCompletionCandidates::class,
    )
    var filterRepository: String? = null

    override fun run() {
        val repositoryName = requireNotNull(filterRepository ?: retContext.gitRepository) {
            "Could not determine repository from context. Please provide the repository."
        }
        val contextBranch = retContext.gitBranch
        val sourceBranch = providedBranch ?: contextBranch

        if (!noPrompt) {
            val branch = if (autofillBranchRequired(filterRepository, providedBranch, contextBranch)) sourceBranch else null

            val prCreateURL = gitProvider.urlFactory.createPullRequestCreateUrl(repositoryName, branch)
            browserUtils.openUrl(prCreateURL)
        } else {
            requireNotNull(sourceBranch) { "Could not determine branch from context. Please provide the branch." }
            val repository = gitProvider.getRepositoryById(repositoryName)
            val defaultBranch = requireNotNull(repository.defaultBranch) { "No default branch available." }

            require(defaultBranch != sourceBranch) {
                "Could not create PR. Source branch is the same as the default branch."
            }

            try {
                val createPullRequestResponse = gitProvider.createPullRequest(
                    repositoryName,
                    "refs/heads/$sourceBranch",
                    defaultBranch,
                    "Merge $sourceBranch into ${repository.defaultBranch}",
                    "PR created by RET using `ret pr create --no-prompt`.",
                )
                val pullRequestUrl = gitProvider.urlFactory.pullRequestUrl(repositoryName, createPullRequestResponse.pullRequestId)
                outputHandler.println(pullRequestUrl)
            } catch (e: ClientWebApplicationException) {
                val message = if (e.response.status == CONFLICT) "A pull request for this branch already exists!"
                else "Creating a PR directly failed."

                throw IllegalStateException(message, e)
            }
        }
    }

    private fun autofillBranchRequired(filterRepository: String?, providedBranch: String?, contextBranch: String?): Boolean =
        providedBranch != null || contextBranch != null && filterRepository == null
}

internal class BranchCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_branch").iterator()
}

internal class RepositoryFlagCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_repository_flag").iterator()
}
