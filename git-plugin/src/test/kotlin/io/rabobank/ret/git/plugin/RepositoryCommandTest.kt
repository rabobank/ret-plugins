package io.rabobank.ret.git.plugin

import io.rabobank.ret.RetConsole
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.command.RepositoryCommand
import io.rabobank.ret.git.plugin.config.ExceptionMessageHandler
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.Repository
import io.rabobank.ret.git.plugin.utilities.TestUrlFactory
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.contains
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import picocli.CommandLine
import java.net.URI

internal class RepositoryCommandTest {
    private val mockedGitProvider = mock<GitProvider>()
    private val mockedBrowserUtils = mock<BrowserUtils>()
    private val mockedRetContext = mock<RetContext>()
    private val outputHandler = mock<OutputHandler>()
    private val mockedRetConsole = mock<RetConsole>()
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun before() {
        val command =
            RepositoryCommand(
                mockedGitProvider,
                mockedBrowserUtils,
                mockedRetContext,
            )

        command.contextAwareness = ContextAwareness()

        commandLine = spy(CommandLine(command))
        commandLine.executionExceptionHandler = ExceptionMessageHandler(outputHandler)

        whenever(mockedGitProvider.getAllRepositories()).thenReturn(
            listOf(
                Repository("client-service", "refs/heads/master"),
                Repository("admin-service", "refs/heads/master"),
                Repository("bto-apmd", "refs/heads/master"),
                Repository("open-source-tool", "refs/heads/master"),
                Repository("generic-project", "refs/heads/master"),
            ),
        )
        whenever(mockedGitProvider.urlFactory).thenReturn(TestUrlFactory("https://test.git"))
    }

    @AfterEach
    fun afterEach() {
        verifyNoMoreInteractions(mockedRetConsole)
    }

    @ParameterizedTest
    @ValueSource(strings = ["admin-service", "bto-apmd", "open-source-tool"])
    fun `repository open command should open browser with repository url`(repository: String) {
        val exitCode = commandLine.execute("open", repository)
        assertThat(exitCode).isEqualTo(0)

        val repoUrl = URI.create("https://test.git/repository/$repository").toURL()

        verify(mockedBrowserUtils).openUrl(repoUrl)
    }

    @Test
    fun `repository open command without argument should use context awareness`() {
        val repository = "generic-project"
        whenever(mockedRetContext.gitRepository).thenReturn(repository)

        val exitCode = commandLine.execute("open")
        assertThat(exitCode).isEqualTo(0)

        val repoUrl = URI.create("https://test.git/repository/$repository").toURL()

        verify(mockedBrowserUtils).openUrl(repoUrl)
    }

    @Test
    fun `repository open command with argument and context awareness should use argument`() {
        val repository = "open-source-tool"
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        val exitCode = commandLine.execute("open", repository)
        assertThat(exitCode).isEqualTo(0)

        val repoUrl = URI.create("https://test.git/repository/$repository").toURL()

        verify(mockedBrowserUtils).openUrl(repoUrl)
    }

    @Test
    fun `repository open command without argument and no context awareness should log`() {
        val exitCode = commandLine.execute("open")
        assertThat(exitCode).isEqualTo(2)

        verify(outputHandler).error("No repository provided and ret cannot get repository from context.")
        verify(outputHandler).error(contains("Usage:"))
    }

    @Test
    fun `repository open command should log if repository does not exists`() {
        val repositoryThatDoesNotExist = "bto-generic-source-admin-gateway"
        val exitCode = commandLine.execute("open", repositoryThatDoesNotExist)
        assertThat(exitCode).isEqualTo(2)

        verify(outputHandler).error("No repository found with name bto-generic-source-admin-gateway.")
        verify(outputHandler).error(contains("Usage:"))
    }
}
