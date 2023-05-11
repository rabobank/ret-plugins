package io.rabobank.ret.git.plugin.output

import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.azure.Branch
import io.rabobank.ret.git.plugin.azure.Pipeline
import io.rabobank.ret.git.plugin.azure.PipelineRun
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository

class CliOutputHandler(private val retConsole: RetConsole) : OutputHandler {

    override fun println(message: String) {
        retConsole.out(message)
    }

    override fun error(message: String) {
        retConsole.errorOut(message)
    }

    override fun listPRs(list: List<PullRequest>) {
        throw UnsupportedOperationException()
    }

    override fun listRepositories(list: List<Repository>) {
        throw UnsupportedOperationException()
    }

    override fun listBranches(list: List<Branch>) {
        throw UnsupportedOperationException()
    }

    override fun listPipelines(list: List<Pipeline>) {
        throw UnsupportedOperationException()
    }

    override fun listPipelineRuns(list: List<PipelineRun>) {
        throw UnsupportedOperationException()
    }
}
