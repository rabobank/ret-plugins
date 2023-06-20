package io.rabobank.ret.git.plugin.provider

interface GitProvider {
    fun getAllPullRequests(): List<PullRequest>

    fun getPullRequestById(id: String): PullRequest

    fun createPullRequest(repository: String, apiVersion: String, createPullRequest: CreatePullRequest): PullRequestCreated

    fun getAllRepositories(): List<Repository>

    fun getRepositoryById(repository: String): Repository

    fun getAllRefs(repository: String, filter: String): List<Branch>

    fun getAllPipelines(): List<Pipeline>

    fun getPipelineRuns(pipelineId: String): List<PipelineRun>

    fun getUrlFactory(): GitUrlFactory
}