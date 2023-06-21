package io.rabobank.ret.git.plugin.provider

import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsPluginConfig
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsProvider
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsUrlFactory
import io.rabobank.ret.git.plugin.provider.github.GitHubClient
import io.rabobank.ret.git.plugin.provider.github.GitHubProvider
import io.rabobank.ret.git.plugin.provider.github.GitHubUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class GitProviderSelector(
    private val azureDevopsClient: AzureDevopsClient,
    private val pluginConfig: AzureDevopsPluginConfig,
    private val azureDevopsUrlFactory: AzureDevopsUrlFactory,

    private val gitHubClient: GitHubClient
) {

    // TODO make selection better
    @Produces
    fun gitProvider(): GitProvider {
        //AzureDevopsProvider(azureDevopsClient, pluginConfig, azureDevopsUrlFactory)
        return GitHubProvider(gitHubClient, GitHubUrlFactory())
    }
}