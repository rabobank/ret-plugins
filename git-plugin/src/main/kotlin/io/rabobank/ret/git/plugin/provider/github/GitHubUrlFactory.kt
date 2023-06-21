package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.git.plugin.provider.GitUrlFactory

class GitHubUrlFactory : GitUrlFactory {
    override fun createRepositoryUrl(repositoryName: String): String {
        TODO("Not yet implemented")
    }

    override fun createPipelineRunUrl(pipelineRunId: String): String {
        TODO("Not yet implemented")
    }

    override fun createPipelineUrl(pipelineId: String): String {
        TODO("Not yet implemented")
    }

    override fun createPipelineDashboardUrl(): String {
        TODO("Not yet implemented")
    }

    override fun createPullRequestUrl(repositoryName: String, pullRequestId: String): String {
        TODO("Not yet implemented")
    }

    override fun createPullRequestCreateUrl(repositoryName: String, sourceRef: String?): String {
        TODO("Not yet implemented")
    }

    override fun pullRequestUrl(repositoryName: String, pullRequestId: String): String {
        TODO("Not yet implemented")
    }

}
