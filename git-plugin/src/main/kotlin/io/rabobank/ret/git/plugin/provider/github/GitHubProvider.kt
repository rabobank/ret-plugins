package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.git.plugin.provider.*
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.PullRequestCreated
import io.rabobank.ret.git.plugin.provider.Repository

class GitHubProvider(
    private val gitHubClient: GitHubClient,
    override val urlFactory: GitUrlFactory
) : GitProvider {
    override fun getAllPullRequests(): List<PullRequest> {
        TODO("Not yet implemented")
    }

    override fun getPullRequestsNotReviewedByUser(): List<PullRequest> {
        TODO("Not yet implemented")
    }

    override fun getPullRequestById(id: String): PullRequest {
        TODO("Not yet implemented")
    }

    override fun createPullRequest(repository: String, sourceRefName: String, targetRefName: String, title: String, description: String): PullRequestCreated {
        TODO("Not yet implemented")
    }

    override fun getAllRepositories(): List<Repository> {
        return gitHubClient.getRepositories().toGenericDomain()
    }

    override fun getRepositoryById(repository: String): Repository {
        TODO("Not yet implemented")
    }

    override fun getAllRefs(repository: String, filter: String): List<Branch> {
        TODO("Not yet implemented")
    }

    override fun getAllPipelines(): List<Pipeline> {
        TODO("Not yet implemented")
    }

    override fun getPipelineRuns(pipelineId: String): List<PipelineRun> {
        TODO("Not yet implemented")
    }
}