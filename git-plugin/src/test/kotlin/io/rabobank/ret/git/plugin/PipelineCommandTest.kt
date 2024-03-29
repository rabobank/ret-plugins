package io.rabobank.ret.git.plugin

import io.rabobank.ret.git.plugin.command.PipelineCommand
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.utilities.TestUrlFactory
import io.rabobank.ret.util.BrowserUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import java.net.URI
import java.net.URL

internal class PipelineCommandTest {
    private val browserUtilsMock = mock<BrowserUtils>()
    private val gitProviderMock = mock<GitProvider>()
    private lateinit var gitUrlFactory: GitUrlFactory
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun before() {
        gitUrlFactory = TestUrlFactory("https://test.git")
        val command = PipelineCommand(browserUtilsMock, gitProviderMock)

        commandLine = CommandLine(command)

        whenever(gitProviderMock.urlFactory).thenReturn(gitUrlFactory)
    }

    @Test
    fun `should open browser for the pipeline dashboard`() {
        val expectedPipelineRunURL = URI.create("https://test.git/pipeline").toURL()

        commandLine.execute("open")

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }

    @Test
    fun `should open browser for correct pipeline`() {
        val pipelineId = "123"
        val expectedPipelineRunURL = URI.create("https://test.git/pipeline/$pipelineId").toURL()

        commandLine.execute("open", pipelineId)

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }

    @Test
    fun `should open browser for correct pipeline by folder and name`() {
        val pipelineId = "folder\\pipeline_name"
        val expectedPipelineRunURL = URI.create("https://test.git/pipeline/123").toURL()
        whenever(gitProviderMock.getAllPipelines()).thenReturn(
            listOf(
                Pipeline(123, "pipeline_name", "folder", "folder\\pipeline_name"),
                Pipeline(234, "other_pipeline_name", "folder", "folder\\other_pipeline_name"),
                Pipeline(345, "pipeline_name", "folder2", "folder2\\pipeline_name"),
            ),
        )

        commandLine.execute("open", pipelineId)

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }

    @Test
    fun `should not open browser for pipeline by folder and name when name is incorrect`() {
        val pipelineId = "folder/pipeline_name2"
        whenever(gitProviderMock.getAllPipelines()).thenReturn(
            listOf(
                Pipeline(123, "pipeline_name", "folder", "folder\\pipeline_name"),
            ),
        )

        commandLine.execute("open", pipelineId)

        verify(browserUtilsMock, never()).openUrl(any<URL>())
    }

    @Test
    fun `should open browser for correct pipeline run`() {
        val pipelineId = "not_used"
        val pipelineRunId = "123456"
        val expectedPipelineRunURL =
            URI.create("https://test.git/pipeline/run/$pipelineRunId").toURL()

        commandLine.execute("open", pipelineId, pipelineRunId)

        verify(browserUtilsMock).openUrl(expectedPipelineRunURL)
    }
}
