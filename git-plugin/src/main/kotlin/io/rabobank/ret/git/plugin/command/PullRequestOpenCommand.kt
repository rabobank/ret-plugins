package io.rabobank.ret.git.plugin.command

import io.quarkus.logging.Log
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.Logged
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ScopeType

@Command(
    name = "open",
    description = ["Navigate to a pull request in Git"],
)
@Logged
class PullRequestOpenCommand(
    private val gitProvider: GitProvider,
    private val browserUtils: BrowserUtils,
    private val outputHandler: OutputHandler,
) : Runnable {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Option(
        names = ["--repository", "-r"],
        description = ["Filter on repository"],
        scope = ScopeType.INHERIT,
        completionCandidates = RepositoryFlagCompletionCandidates::class,
    )
    var filterRepository: String? = null

    @Parameters(
        arity = "1",
        completionCandidates = PullRequestCompletionCandidates::class,
        description = ["Pull Request ID"],
    )
    var pullRequestId: String = ""

    override fun run() {
        try {
            val pullRequest = gitProvider.getPullRequestById(pullRequestId)
            val prURL = gitProvider.urlFactory.pullRequest(pullRequest.repository.name, pullRequest.id)

            browserUtils.openUrl(prURL)
        } catch (e: ClientWebApplicationException) {
            if (e.response.status == NOT_FOUND) {
                outputHandler.error("Pull request with id '$pullRequestId' could not be found")
            } else {
                outputHandler.error("Something failed when fetching pull request with id: $pullRequestId")
            }

            Log.error("Unable to open pr with id $pullRequestId", e)
            // Only for causing an exitcode "1", exitProcess does not have the same effect
            throw e
        }
    }
}

internal class PullRequestCompletionCandidates : Iterable<String> {
    override fun iterator() = listOf("function:_autocomplete_pullrequest").iterator()
}
