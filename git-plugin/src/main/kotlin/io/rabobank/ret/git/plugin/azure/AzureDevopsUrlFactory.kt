package io.rabobank.ret.git.plugin.azure

import io.rabobank.ret.git.plugin.config.PluginConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class AzureDevopsUrlFactory(
    private val pluginConfig: PluginConfig,
    @ConfigProperty(name = "azure.devops.baseUrl") private val azureDevopsBaseUrl: String,
) {

    fun createRepositoryUrl(repositoryName: String): String {
        return azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .build()
            .toASCIIString()
    }

    fun createPipelineRunUrl(pipelineRunId: String): String {
        return azdoBaseUriBuilder()
            .path("_build")
            .path("results")
            .queryParam("buildId", pipelineRunId)
            .build()
            .toASCIIString()
    }

    fun createPipelineUrl(pipelineId: String): String {
        return azdoBaseUriBuilder()
            .path("_build")
            .queryParam("definitionId", pipelineId)
            .build()
            .toASCIIString()
    }

    fun createPipelineDashboardUrl(): String {
        return azdoBaseUriBuilder()
            .path("_build")
            .build()
            .toASCIIString()
    }

    fun createPullRequestUrl(repositoryName: String, pullRequestId: String): String {
        return azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequest")
            .path(pullRequestId)
            .build()
            .toASCIIString()
    }

    fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): String {
        return azdoBaseUriBuilder()
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
    }

    fun pullRequestUrl(repositoryName: String, pullRequestId: String): String {
        return azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequest")
            .path(pullRequestId)
            .build()
            .toASCIIString()
    }

    private fun azdoBaseUriBuilder() = UriBuilder.fromUri(azureDevopsBaseUrl)
        .path(pluginConfig.organization)
        .path(pluginConfig.projectId)
}
