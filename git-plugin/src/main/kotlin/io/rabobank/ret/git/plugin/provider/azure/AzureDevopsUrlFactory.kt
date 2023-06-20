package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class AzureDevopsUrlFactory(
    private val pluginConfig: AzureDevopsPluginConfig,
    @ConfigProperty(name = "azure.devops.baseUrl") private val azureDevopsBaseUrl: String,
) : GitUrlFactory {

    override fun createRepositoryUrl(repositoryName: String): String =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .build()
            .toASCIIString()

    override fun createPipelineRunUrl(pipelineRunId: String): String =
        azdoBaseUriBuilder()
            .path("_build")
            .path("results")
            .queryParam("buildId", pipelineRunId)
            .build()
            .toASCIIString()

    override fun createPipelineUrl(pipelineId: String): String =
        azdoBaseUriBuilder()
            .path("_build")
            .queryParam("definitionId", pipelineId)
            .build()
            .toASCIIString()

    override fun createPipelineDashboardUrl(): String =
        azdoBaseUriBuilder()
            .path("_build")
            .build()
            .toASCIIString()

    override fun createPullRequestUrl(repositoryName: String, pullRequestId: String): String =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequest")
            .path(pullRequestId)
            .build()
            .toASCIIString()

    override fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): String =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequestcreate")
            .also {
                if (sourceRef != null) {
                    it.queryParam("sourceRef", sourceRef)
                }
            }
            .build()
            .toASCIIString()

    override fun pullRequestUrl(repositoryName: String, pullRequestId: String): String =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequest")
            .path(pullRequestId)
            .build()
            .toASCIIString()

    private fun azdoBaseUriBuilder() = UriBuilder.fromUri(azureDevopsBaseUrl)
        .path(pluginConfig.organization)
        .path(pluginConfig.projectId)
}
