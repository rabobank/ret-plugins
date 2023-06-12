package io.rabobank.ret.git.plugin

import io.quarkus.test.junit.QuarkusTest
import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.RetConfig
import io.rabobank.ret.git.plugin.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.azure.AzureDevopsUrlFactory
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository
import io.rabobank.ret.git.plugin.azure.Reviewer
import io.rabobank.ret.git.plugin.command.PullRequestOpenCommand
import io.rabobank.ret.git.plugin.config.PluginConfig
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.inject.Instance
import org.assertj.core.api.Assertions.assertThat
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine

private const val AZURE_DEVOPS_BASE_URL = "azdo.com"

@QuarkusTest
internal class PullRequestOpenCommandTest {

    private val mockedAzureDevopsClient = mock<AzureDevopsClient>()
    private val mockedBrowserUtils = mock<BrowserUtils>()
    private val outputHandler = mock<OutputHandler>()
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun before() {
        val configurables = mock<Instance<Configurable>>()
        val retConfig = RetConfig(OsUtils(), configurables, "1.0.0")
        retConfig["azure_devops_email"] = "manks@live.com"
        retConfig["azure_devops_pat"] = "pat"
        retConfig["azure_devops_project"] = "projectId"
        retConfig["azure_devops_organization"] = "organization"

        val command = PullRequestOpenCommand(
            mockedAzureDevopsClient,
            AzureDevopsUrlFactory(PluginConfig(retConfig), "azdo.com"),
            mockedBrowserUtils,
            outputHandler,
        )

        command.contextAwareness = ContextAwareness()

        commandLine = spy(CommandLine(command))
    }

    @Test
    fun pullRequestsCanBeOpened() {
        whenever(mockedAzureDevopsClient.getPullRequestById("1234")).thenReturn(
            PullRequest(
                "1234",
                "PR Title",
                Repository("repo", "refs/heads/master"),
                listOf(Reviewer("manks@live.com")),
            ),
        )

        val exitCode = commandLine.execute("1234")
        assertThat(exitCode).isEqualTo(0)
        val expectedURL = "$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/repo/pullrequest/1234"

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun pullRequestsCannotBeOpenedWhenItDoesNotExist() {
        whenever(mockedAzureDevopsClient.getPullRequestById("12345")).thenThrow(
            ClientWebApplicationException(404),
        )

        val exitCode = commandLine.execute("12345")
        assertThat(exitCode).isEqualTo(1)
        verify(outputHandler).error("Pull request with id '12345' could not be found")
    }
}
