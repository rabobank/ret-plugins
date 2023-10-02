package io.rabobank.ret.git.plugin.output

import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository

interface OutputHandler {
    fun println(message: String) {
        // No-op
    }

    fun error(message: String) {
        // No-op
    }

    fun listPRs(list: List<PullRequest>)

    fun listRepositories(list: List<Repository>)

    fun listBranches(list: List<Branch>)

    fun listPipelines(list: List<Pipeline>)

    fun listPipelineRuns(list: List<PipelineRun>)
}
