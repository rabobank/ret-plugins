package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsUrlFactory
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

@Command(
    name = "repository",
    description = ["List all repositories"],
)
class RepositoryCommand(
    private val azureDevopsClient: AzureDevopsClient,
    private val azureDevopsUrlFactory: AzureDevopsUrlFactory,
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
) {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "open")
    fun openRepositoryInBrowser(
        @Parameters(
            arity = "0..1",
            description = ["Repository name to open in Azure DevOps"],
            paramLabel = "<repository>",
            completionCandidates = RepositoryCompletionCandidates::class,
        ) repositoryName: String?,
    ) {
        val repository = requireNotNull(repositoryName ?: retContext.gitRepository) {
            "No repository provided and ret cannot get repository from context."
        }

        val repositories = azureDevopsClient.getAllRepositories().value

        require(repositories.any { it.name == repository }) { "No repository found with name $repository." }

        browserUtils.openUrl(azureDevopsUrlFactory.createRepositoryUrl(repository))
    }
}

internal class RepositoryCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_repository").iterator()
}
