package io.rabobank.ret.git.plugin

import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsUrlFactory
import io.rabobank.ret.git.plugin.provider.azure.AzureResponse
import io.rabobank.ret.git.plugin.provider.azure.Pipeline
import io.rabobank.ret.git.plugin.command.PipelineCommand
import io.rabobank.ret.git.plugin.config.PluginConfig
import io.rabobank.ret.util.BrowserUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine

internal class PipelineCommandTest {

    private val pluginConfigMock = mock<PluginConfig>()
    private val browserUtilsMock = mock<BrowserUtils>()
    private val azureDevopsClientMock = mock<AzureDevopsClient>()
    private lateinit var azureDevopsUrlFactory: AzureDevopsUrlFactory
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun before() {
        azureDevopsUrlFactory = AzureDevopsUrlFactory(pluginConfigMock, "https://dev.azure.com/my-organization")
        val command = PipelineCommand(azureDevopsUrlFactory, browserUtilsMock, azureDevopsClientMock)

        commandLine = CommandLine(command)

        whenever(pluginConfigMock.organization).thenReturn("org")
        whenever(pluginConfigMock.projectId).thenReturn("proj")
    }

    @Test
    fun `should open browser for the pipeline dashboard`() {
        val expectedPipelineRunURL = "https://dev.azure.com/my-organization/org/proj/_build"

        commandLine.execute("open")

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }

    @Test
    fun `should open browser for correct pipeline`() {
        val pipelineId = "123"
        val expectedPipelineRunURL = "https://dev.azure.com/my-organization/org/proj/_build?definitionId=$pipelineId"

        commandLine.execute("open", pipelineId)

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }

    @Test
    fun `should open browser for correct pipeline by folder and name`() {
        val pipelineId = "folder\\pipeline_name"
        val expectedPipelineRunURL = "https://dev.azure.com/my-organization/org/proj/_build?definitionId=123"
        whenever(azureDevopsClientMock.getAllPipelines()).thenReturn(
            AzureResponse.of(
                Pipeline(123, "pipeline_name", "\\folder"),
                Pipeline(234, "other_pipeline_name", "\\folder"),
                Pipeline(345, "pipeline_name", "\\folder2"),
            ),
        )

        commandLine.execute("open", pipelineId)

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }

    @Test
    fun `should not open browser for pipeline by folder and name when name is incorrect`() {
        val pipelineId = "folder/pipeline_name2"
        whenever(azureDevopsClientMock.getAllPipelines()).thenReturn(
            AzureResponse.of(
                Pipeline(123, "pipeline_name", "\\folder"),
            ),
        )

        commandLine.execute("open", pipelineId)

        verify(browserUtilsMock, never()).openUrl(any())
    }

    @Test
    fun `should open browser for correct pipeline run`() {
        val pipelineId = "not_used"
        val pipelineRunId = "123456"
        val expectedPipelineRunURL =
            "https://dev.azure.com/my-organization/org/proj/_build/results?buildId=$pipelineRunId"

        commandLine.execute("open", pipelineId, pipelineRunId)

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }
}
