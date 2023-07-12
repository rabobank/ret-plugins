package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URL

@ApplicationScoped
class AzureDevopsUrlFactory(
    private val pluginConfig: AzureDevopsPluginConfig,
    @ConfigProperty(name = "azure.devops.baseUrl") private val azureDevopsBaseUrl: String,
) : GitUrlFactory {

    override fun createRepositoryUrl(repositoryName: String): URL =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .buildToURL()

    override fun createPipelineRunUrl(pipelineRunId: String): URL =
        azdoBaseUriBuilder()
            .path("_build")
            .path("results")
            .queryParam("buildId", pipelineRunId)
            .buildToURL()

    override fun createPipelineUrl(pipelineId: String): URL =
        azdoBaseUriBuilder()
            .path("_build")
            .queryParam("definitionId", pipelineId)
            .buildToURL()

    override fun createPipelineDashboardUrl(): URL =
        azdoBaseUriBuilder()
            .path("_build")
            .buildToURL()

    override fun createPullRequestUrl(repositoryName: String, pullRequestId: String): URL =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequest")
            .path(pullRequestId)
            .buildToURL()

    override fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): URL =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequestcreate")
            .also {
                if (sourceRef != null) {
                    it.queryParam("sourceRef", sourceRef)
                }
            }
            .buildToURL()

    private fun azdoBaseUriBuilder() = UriBuilder.fromUri(azureDevopsBaseUrl)
        .path(pluginConfig.organization)
        .path(pluginConfig.projectId)

    private fun UriBuilder.buildToURL() = this.build().toURL()
}
