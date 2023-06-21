package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.UriBuilder

private const val BASE_URI = "https://www.github.com"

@ApplicationScoped
class GitHubUrlFactory(private val pluginConfig: GitHubPluginConfig) : GitUrlFactory {

    override fun createRepositoryUrl(repositoryName: String): String = baseBuilder
        .path(pluginConfig.organization)
        .path(repositoryName)
        .build()
        .toASCIIString()

    override fun createPipelineRunUrl(pipelineRunId: String): String {
        TODO("Not yet implemented")
    }

    override fun createPipelineUrl(pipelineId: String): String {
        TODO("Not yet implemented")
    }

    override fun createPipelineDashboardUrl(): String {
        TODO("Not yet implemented")
    }

    override fun createPullRequestUrl(repositoryName: String, pullRequestId: String): String = baseBuilder
        .path(pluginConfig.organization)
        .path(repositoryName)
        .path("pull")
        .path(pullRequestId)
        .build()
        .toASCIIString()

    override fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): String = baseBuilder
        .path(pluginConfig.organization)
        .path(repositoryName)
        .path("compare")
        .also {
            if (sourceRef != null) {
                it.path("master...$sourceRef") // TODO - get master from default_branch
            }
        }
        .build()
        .toASCIIString()

    private val baseBuilder = UriBuilder.fromUri(BASE_URI)
}
