package io.rabobank.ret.git.plugin.provider

import java.net.URL

interface GitUrlFactory {
    fun repository(repositoryName: String): URL
    fun pipelineRun(pipelineRunId: String): URL
    fun pipeline(pipelineId: String): URL
    fun pipelineDashboard(): URL
    fun pullRequest(repositoryName: String, pullRequestId: String): URL
    fun pullRequestCreate(repositoryName: String, sourceRef: String?): URL
}
