package io.rabobank.ret.git.plugin.utilities

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.ws.rs.core.UriBuilder
import java.net.URL

class TestUrlFactory(private val domain: String) : GitUrlFactory {
    override fun createRepositoryUrl(repositoryName: String) = "$domain/repository/$repositoryName".toURL()
    override fun createPipelineRunUrl(pipelineRunId: String) = "$domain/pipeline/run/$pipelineRunId".toURL()
    override fun createPipelineUrl(pipelineId: String) = "$domain/pipeline/$pipelineId".toURL()
    override fun createPipelineDashboardUrl() = "$domain/pipeline".toURL()
    override fun createPullRequestUrl(repositoryName: String, pullRequestId: String) = "$domain/pullrequest/$repositoryName/$pullRequestId".toURL()
    override fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?) = ("$domain/pullrequest/create/$repositoryName/" + (sourceRef ?: "")).toURL()

    private fun String.toURL(): URL = UriBuilder.fromUri(this).build().toURL()
}