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
    private val pluginConfig: GitHubPluginConfig,
    override val urlFactory: GitUrlFactory
) : GitProvider {
    override fun getAllPullRequests(): List<PullRequest> {
        return gitHubClient.searchIssuesForPullRequests("org:${pluginConfig.organization} is:pull-request state:open").items.toGenericDomain()
    }

    override fun getPullRequestsNotReviewedByUser(): List<PullRequest> {
        // TODO we probably have to search all pull requests and then per PR find the reviewers in a specific call
        TODO("Not yet implemented")
    }

    override fun getPullRequestById(id: String): PullRequest {
        // TODO - the repository name is needed here, as PR numbers/IDs are not unique in the whole organization
        TODO("Not yet implemented")
    }

    override fun createPullRequest(repository: String, sourceRefName: String, targetRefName: String, title: String, description: String): PullRequestCreated {
        TODO("Not yet implemented")
    }

    override fun getAllRepositories(): List<Repository> {
        return gitHubClient.getRepositories(pluginConfig.organization).toGenericDomain()
    }

    override fun getRepositoryById(repository: String): Repository {
        return gitHubClient.getRepository(pluginConfig.organization, repository).toGenericDomain()
    }

    override fun getAllRefs(repository: String, filter: String): List<Branch> {
        return gitHubClient.getBranches(pluginConfig.organization, repository).toGenericDomain()
        // TODO implement filter
    }

    override fun getAllPipelines(): List<Pipeline> {
        TODO("Not yet implemented")
    }

    override fun getPipelineRuns(pipelineId: String): List<PipelineRun> {
        TODO("Not yet implemented")
    }
}