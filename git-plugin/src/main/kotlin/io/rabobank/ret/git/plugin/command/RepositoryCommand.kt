package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.Logged
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

@Command(
    name = "repository",
    description = ["List all repositories"],
)
@Logged
class RepositoryCommand(
    private val gitProvider: GitProvider,
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
) {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "open")
    fun openRepositoryInBrowser(
        @Parameters(
            arity = "0..1",
            description = ["Repository name to open in the browser"],
            paramLabel = "<repository>",
            completionCandidates = RepositoryCompletionCandidates::class,
        ) repositoryName: String?,
    ) {
        val repository =
            requireNotNull(repositoryName ?: retContext.gitRepository) {
                "No repository provided and ret cannot get repository from context."
            }

        val repositories = gitProvider.getAllRepositories()

        require(repositories.any { it.name == repository }) { "No repository found with name $repository." }

        val url = gitProvider.urlFactory.repository(repository)
        browserUtils.openUrl(url)
    }
}

internal class RepositoryCompletionCandidates : Iterable<String> {
    override fun iterator() = listOf("function:_autocomplete_repository").iterator()
}
