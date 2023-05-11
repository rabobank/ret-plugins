package io.rabobank.ret.git.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.azure.Branch
import io.rabobank.ret.git.plugin.azure.Pipeline
import io.rabobank.ret.git.plugin.azure.PipelineRun
import io.rabobank.ret.git.plugin.azure.PipelineRunResult
import io.rabobank.ret.git.plugin.azure.PipelineRunState
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository

class AlfredAutocompleteHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) :
    OutputHandler {
    override fun println(message: String) {
        throw UnsupportedOperationException()
    }

    override fun error(message: String) {
        throw UnsupportedOperationException()
    }

    override fun listPRs(list: List<PullRequest>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No pull requests found", valid = false)))
                    else -> Wrapper(list.map { Item(it) })
                },
            ),
        )
    }

    override fun listRepositories(list: List<Repository>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No repositories found", valid = false)))
                    else -> Wrapper(list.map { Item(it) })
                },
            ),
        )
    }

    override fun listBranches(list: List<Branch>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No branches found", valid = false)))
                    else -> Wrapper(list.map { Item(it) })
                },
            ),
        )
    }

    override fun listPipelines(list: List<Pipeline>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No pipelines found", valid = false)))
                    else -> Wrapper(
                        listOf(Item(title = "Pipeline dashboard", arg = "open-dashboard")) +
                            list.map { Item(title = it.name, subtitle = "Folder: ${it.folder}", arg = it.id.toString()) },
                    )
                },
            ),
        )
    }

    override fun listPipelineRuns(list: List<PipelineRun>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No pipeline runs found", valid = false)))
                    else -> Wrapper(
                        listOf(Item(title = "Pipeline run overview", arg = "open-dashboard")) +
                            list.map {
                                Item(
                                    title = it.name,
                                    subtitle = "State: ${it.state}, result: ${it.result}",
                                    icon = ItemIcon("icons/${it.icon()}"),
                                    arg = it.id.toString(),
                                )
                            },
                    )
                },
            ),
        )
    }

    private fun PipelineRun.icon() = when (state) {
        PipelineRunState.COMPLETED -> when (result) {
            PipelineRunResult.SUCCEEDED -> "succeeded.png"
            else -> "failed.png"
        }

        else -> "in_progress.png"
    }
}
