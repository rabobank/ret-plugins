package io.rabobank.ret.git.plugin.output

import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.azure.Branch
import io.rabobank.ret.git.plugin.azure.Pipeline
import io.rabobank.ret.git.plugin.azure.PipelineRun
import io.rabobank.ret.git.plugin.azure.PipelineRunState
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository

class CliAutocompleteHandler(private val retConsole: RetConsole) : OutputHandler {
    override fun listPRs(list: List<PullRequest>) {
        list.map { "${it.id}:${it.repository.name}: ${it.title}" }.forEach(retConsole::out)
    }

    override fun listRepositories(list: List<Repository>) {
        list.map { it.name }.forEach(retConsole::out)
    }

    override fun listBranches(list: List<Branch>) {
        list.map { it.shortName }.forEach(retConsole::out)
    }

    override fun listPipelines(list: List<Pipeline>) {
        list.map { it.uniqueName }.forEach(retConsole::out)
    }

    override fun listPipelineRuns(list: List<PipelineRun>) {
        list.map {
            val combinedState = when (it.state) {
                PipelineRunState.COMPLETED -> it.result
                else -> it.state
            }
            "${it.id}:${it.name} ($combinedState)"
        }.forEach(retConsole::out)
    }
}
