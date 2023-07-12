package io.rabobank.ret.git.plugin.provider

import java.net.URL

interface GitUrlFactory {
    fun createRepositoryUrl(repositoryName: String): URL
    fun createPipelineRunUrl(pipelineRunId: String): URL
    fun createPipelineUrl(pipelineId: String): URL
    fun createPipelineDashboardUrl(): URL
    fun createPullRequestUrl(repositoryName: String, pullRequestId: String): URL
    fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): URL
    fun pullRequestUrl(repositoryName: String, pullRequestId: String): URL
}