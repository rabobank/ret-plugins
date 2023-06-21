package io.rabobank.ret.git.plugin.provider

interface GitUrlFactory {
    fun createRepositoryUrl(repositoryName: String): String
    fun createPipelineRunUrl(pipelineRunId: String): String
    fun createPipelineUrl(pipelineId: String): String
    fun createPipelineDashboardUrl(): String
    fun createPullRequestUrl(repositoryName: String, pullRequestId: String): String
    fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): String
}