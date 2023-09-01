package io.rabobank.ret.git.plugin.utilities

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.ws.rs.core.UriBuilder
import java.net.URL

class TestUrlFactory(private val domain: String) : GitUrlFactory {
    override fun repository(repositoryName: String) = "$domain/repository/$repositoryName".toURL()
    override fun pipelineRun(pipelineRunId: String) = "$domain/pipeline/run/$pipelineRunId".toURL()
    override fun pipeline(pipelineId: String) = "$domain/pipeline/$pipelineId".toURL()
    override fun pipelineDashboard() = "$domain/pipeline".toURL()
    override fun pullRequest(
        repositoryName: String,
        pullRequestId: String,
    ) = "$domain/pullrequest/$repositoryName/$pullRequestId".toURL()

    override fun pullRequestCreate(
        repositoryName: String,
        sourceRef: String?,
    ) = ("$domain/pullrequest/create/$repositoryName/" + (sourceRef ?: "")).toURL()

    private fun String.toURL(): URL = UriBuilder.fromUri(this).build().toURL()
}
