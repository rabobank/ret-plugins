package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.PullRequestCreated

const val API_VERSION = "6.0"

class AzureDevopsProvider(
    private val azureDevopsClient: AzureDevopsClient,
    private val pluginConfig: AzureDevopsPluginConfig,
    override val urlFactory: AzureDevopsUrlFactory,
) : GitProvider {

    override fun getAllPullRequests() =
        azureDevopsClient.getAllPullRequests().value.toGenericDomain()

    override fun getPullRequestsNotReviewedByUser() =
        getAllPullRequests().filterNot {
            it.reviewers.any { reviewer -> reviewer.uniqueName.equals(pluginConfig.config.email, true) }
        }

    override fun getPullRequestById(id: String) =
        azureDevopsClient.getPullRequestById(id).toGenericDomain()

    override fun createPullRequest(
        repository: String,
        sourceRefName: String,
        targetRefName: String,
        title: String,
        description: String,
    ): PullRequestCreated {
        val creationDTO = CreatePullRequest(sourceRefName, targetRefName, title, description)
        return azureDevopsClient.createPullRequest(repository, API_VERSION, creationDTO).toGenericDomain()
    }

    override fun getAllRepositories() =
        azureDevopsClient.getAllRepositories().value.toGenericDomain()

    override fun getRepositoryById(repository: String) =
        azureDevopsClient.getRepositoryById(repository).toGenericDomain()

    override fun getAllRefs(repository: String, filter: String) =
        azureDevopsClient.getAllRefs(repository, filter).value.toGenericDomain()

    override fun getAllPipelines() = azureDevopsClient.getAllPipelines().value.toGenericDomain()

    override fun getPipelineRuns(pipelineId: String) =
        azureDevopsClient.getPipelineRuns(pipelineId).value.toGenericDomain()
}
