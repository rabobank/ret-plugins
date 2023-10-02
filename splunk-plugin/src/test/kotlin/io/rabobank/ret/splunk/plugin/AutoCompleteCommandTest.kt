package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.splunk.plugin.output.OutputHandler
import io.rabobank.ret.splunk.plugin.splunk.SplunkPluginConfig
import io.rabobank.ret.splunk.plugin.splunk.SplunkProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine

class AutoCompleteCommandTest {
    private lateinit var commandLine: CommandLine
    private val outputHandler = mock<OutputHandler>()
    private val splunkConfig =
        mock<SplunkPluginConfig> {
            whenever(it.config).thenReturn(mock())
        }

    @BeforeEach
    fun beforeEach() {
        val command = AutoCompleteCommand(splunkConfig, outputHandler, IntelliSearch())
        commandLine = CommandLine(command)
        whenever(splunkConfig.config.indexes).thenReturn(indexes)
        whenever(splunkConfig.config.projects).thenReturn(projects.map(::SplunkProject))
    }

    @Test
    fun `should return all indexes if no word given`() {
        val exitCode = commandLine.execute("indexes")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listIndexes(indexes)
    }

    @ParameterizedTest
    @MethodSource("matchIndexes")
    fun `should return only matched indexes`(
        word: String,
        expectedIndexes: List<String>,
    ) {
        val exitCode = commandLine.execute("indexes", "--word=$word")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listIndexes(expectedIndexes)
    }

    @ParameterizedTest
    @MethodSource("matchProjects")
    fun `should return only matched projects`(
        word: String,
        expectedProjects: List<String>,
    ) {
        val exitCode = commandLine.execute("projects", "--word=$word")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listProjects(expectedProjects)
    }

    companion object {
        @JvmStatic
        fun matchIndexes() =
            listOf(
                Arguments.of("", indexes),
                Arguments.of("1", listOf("index1")),
                Arguments.of("nothing", emptyList<String>()),
            )

        @JvmStatic
        fun matchProjects() =
            listOf(
                Arguments.of("", projects),
                Arguments.of("1", listOf("project1")),
                Arguments.of("nothing", emptyList<String>()),
            )

        private val indexes = listOf("index1", "index2", "my-index")
        private val projects = listOf("project1", "project2", "my-project")
    }
}
