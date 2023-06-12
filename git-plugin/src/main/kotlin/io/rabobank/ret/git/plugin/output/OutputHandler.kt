package io.rabobank.ret.git.plugin.output

import io.rabobank.ret.git.plugin.azure.Branch
import io.rabobank.ret.git.plugin.azure.Pipeline
import io.rabobank.ret.git.plugin.azure.PipelineRun
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository

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
