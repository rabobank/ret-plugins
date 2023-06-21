package io.rabobank.ret.git.plugin.utilities

import io.rabobank.ret.git.plugin.provider.GitUrlFactory

class TestUrlFactory(private val domain: String) : GitUrlFactory {
    override fun createRepositoryUrl(repositoryName: String) = "$domain/repository/$repositoryName"
    override fun createPipelineRunUrl(pipelineRunId: String) = "$domain/pipeline/run/$pipelineRunId"
    override fun createPipelineUrl(pipelineId: String) = "$domain/pipeline/$pipelineId"
    override fun createPipelineDashboardUrl() = "$domain/pipeline"
    override fun createPullRequestUrl(repositoryName: String, pullRequestId: String) = "$domain/pullrequest/$repositoryName/$pullRequestId"
    override fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?) = "$domain/pullrequest/create/$repositoryName/" + (sourceRef ?: "")
}