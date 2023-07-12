package io.rabobank.ret.git.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository

class AlfredOutputHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) : OutputHandler {

    override fun listPRs(list: List<PullRequest>) {
        throw UnsupportedOperationException()
    }

    override fun error(message: String) {
        retConsole.out(objectMapper.writeValueAsString(Wrapper(listOf(Item("Error: $message", false)))))
    }

    override fun println(message: String) {
        retConsole.out(objectMapper.writeValueAsString(Wrapper(listOf(Item(message)))))
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
