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
    override fun repository(repositoryName: String): URL =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .buildToURL()

    override fun pipelineRun(pipelineRunId: String): URL =
        azdoBaseUriBuilder()
            .path("_build")
            .path("results")
            .queryParam("buildId", pipelineRunId)
            .buildToURL()

    override fun pipeline(pipelineId: String): URL =
        azdoBaseUriBuilder()
            .path("_build")
            .queryParam("definitionId", pipelineId)
            .buildToURL()

    override fun pipelineDashboard(): URL =
        azdoBaseUriBuilder()
            .path("_build")
            .buildToURL()

    override fun pullRequest(
        repositoryName: String,
        pullRequestId: String,
    ): URL =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequest")
            .path(pullRequestId)
            .buildToURL()

    override fun pullRequestCreate(
        repositoryName: String,
        sourceRef: String?,
    ): URL =
        azdoBaseUriBuilder()
            .path("_git")
            .path(repositoryName)
            .path("pullrequestcreate")
            .apply {
                if (sourceRef != null) {
                    queryParam("sourceRef", sourceRef)
                }
            }.buildToURL()

    private fun azdoBaseUriBuilder() =
        UriBuilder
            .fromUri(azureDevopsBaseUrl)
            .path(pluginConfig.config.organization)
            .path(pluginConfig.config.project)

    private fun UriBuilder.buildToURL() = this.build().toURL()
}
