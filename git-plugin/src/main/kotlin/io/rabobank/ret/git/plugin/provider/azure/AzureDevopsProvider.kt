package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.*
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.CreatePullRequest
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.PullRequestCreated
import io.rabobank.ret.git.plugin.provider.Repository

class AzureDevopsProvider(private val azureDevopsClient: AzureDevopsClient, override val urlFactory: AzureDevopsUrlFactory) : GitProvider {
    override fun getAllPullRequests(): List<PullRequest> {
        return azureDevopsClient.getAllPullRequests().value.toGenericDomain()
    }

    override fun getPullRequestById(id: String): PullRequest {
        return azureDevopsClient.getPullRequestById(id).toGenericDomain()
    }

    override fun createPullRequest(repository: String, apiVersion: String, createPullRequest: CreatePullRequest): PullRequestCreated {
        return azureDevopsClient.createPullRequest(repository, apiVersion, createPullRequest.fromGenericDomain()).toGenericDomain()
    }

    override fun getAllRepositories(): List<Repository> {
        return azureDevopsClient.getAllRepositories().value.toGenericDomain()
    }

    override fun getRepositoryById(repository: String): Repository {
        return azureDevopsClient.getRepositoryById(repository).toGenericDomain()
    }

    override fun getAllRefs(repository: String, filter: String): List<Branch> {
        return azureDevopsClient.getAllRefs(repository, filter).value.toGenericDomain()
    }

    override fun getAllPipelines(): List<Pipeline> {
        return azureDevopsClient.getAllPipelines().value.toGenericDomain()
    }

    override fun getPipelineRuns(pipelineId: String): List<PipelineRun> {
        return azureDevopsClient.getPipelineRuns(pipelineId).value.toGenericDomain()
    }

}