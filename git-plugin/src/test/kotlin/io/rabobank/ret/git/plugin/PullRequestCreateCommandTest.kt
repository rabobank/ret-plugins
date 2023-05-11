package io.rabobank.ret.git.plugin

import io.quarkus.test.junit.QuarkusTest
import io.rabobank.ret.RetConsole
import io.rabobank.ret.RetContext
import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.RetConfig
import io.rabobank.ret.git.plugin.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.azure.AzureDevopsUrlFactory
import io.rabobank.ret.git.plugin.azure.CreatePullRequest
import io.rabobank.ret.git.plugin.azure.PullRequestCreated
import io.rabobank.ret.git.plugin.azure.Repository
import io.rabobank.ret.git.plugin.command.PullRequestCreateCommand
import io.rabobank.ret.git.plugin.config.ExceptionMessageHandler
import io.rabobank.ret.git.plugin.config.PluginConfig
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.inject.Instance
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.contains
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine

private const val AZURE_DEVOPS_BASE_URL = "azdo.com"

@QuarkusTest
internal class PullRequestCreateCommandTest {

    private lateinit var mockedAzureDevopsClient: AzureDevopsClient
    private lateinit var mockedBrowserUtils: BrowserUtils
    private lateinit var mockedRetContext: RetContext
    private lateinit var commandLine: CommandLine
    private lateinit var outputHandler: OutputHandler
    private lateinit var retConsole: RetConsole

    @BeforeEach
    fun before() {
        val configurables: Instance<Configurable> = mock()
        val retConfig = RetConfig(OsUtils(), configurables, "1.0.0")
        retConfig["azure_devops_email"] = "manks@live.com"
        retConfig["azure_devops_pat"] = "pat"
        retConfig["azure_devops_project"] = "projectId"
        retConfig["azure_devops_organization"] = "organization"

        outputHandler = mock()
        mockedAzureDevopsClient = mock()
        mockedBrowserUtils = mock()
        mockedRetContext = mock()

        val command = PullRequestCreateCommand(
            mockedAzureDevopsClient,
            AzureDevopsUrlFactory(PluginConfig(retConfig), "azdo.com"),
            mockedBrowserUtils,
            outputHandler,
            mockedRetContext,
        )

        command.contextAwareness = ContextAwareness()

        retConsole = mock()
        commandLine = Mockito.spy(CommandLine(command))
        commandLine.executionExceptionHandler = ExceptionMessageHandler(outputHandler)
    }

    @Test
    fun `create should open web page to create pr in context aware case`() {
        val repo = "generic-project"

        whenever(mockedRetContext.gitRepository).thenReturn(repo)
        whenever(mockedRetContext.gitBranch).thenReturn("feature/my-branch")

        val exitCode = commandLine.execute()
        Assertions.assertThat(exitCode).isEqualTo(0)

        val expectedURL =
            "$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/$repo/pullrequestcreate?sourceRef=feature%2Fmy-branch"

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--repository", "-r"])
    fun `create should open web page to create pr when repo and branch are provided`(flag: String) {
        val repo = "generic-project"

        val exitCode = commandLine.execute(flag, repo, "feature/my-branch")
        Assertions.assertThat(exitCode).isEqualTo(0)

        val expectedURL =
            "$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/$repo/pullrequestcreate?sourceRef=feature%2Fmy-branch"

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--repository", "-r"])
    fun `create should open web page to create pr without selected branch, when repo provided and branch not`(flag: String) {
        val repo = "generic-project"

        val exitCode = commandLine.execute(flag, repo)
        Assertions.assertThat(exitCode).isEqualTo(0)

        val expectedURL = "$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/$repo/pullrequestcreate"

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--repository", "-r"])
    fun `create should open web page to create pr without selected branch, when repo provided and branch from context`(
        flag: String,
    ) {
        val repo = "generic-project"

        whenever(mockedRetContext.gitBranch).thenReturn("feature/my-branch")

        val exitCode = commandLine.execute(flag, repo)
        Assertions.assertThat(exitCode).isEqualTo(0)

        val expectedURL = "$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/$repo/pullrequestcreate"

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `create should open web page to create pr when branch provided, repo from context`() {
        val repo = "generic-project"

        whenever(mockedRetContext.gitRepository).thenReturn(repo)

        val exitCode = commandLine.execute("feature/my-branch")
        Assertions.assertThat(exitCode).isEqualTo(0)

        val expectedURL =
            "$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/$repo/pullrequestcreate?sourceRef=feature%2Fmy-branch"

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `create should return error when branch provided, repo not from execution context`() {
        val branch = "feature/my-branch"

        val exitCode = commandLine.execute(branch)
        Assertions.assertThat(exitCode).isEqualTo(2)
        verify(outputHandler).error("Could not determine repository from context. Please provide the repository.")
        verify(outputHandler).error(contains("Usage:"))
    }

    @Test
    fun `create should return a URL to the created PR with --no-prompt`() {
        val branch = "feature/my-branch"
        val repo = "generic-project"
        val defaultBranch = "defaultBranch"
        val createdPullRequestId = "123456"

        whenever(mockedAzureDevopsClient.getRepositoryById(repo)).thenReturn(Repository(repo, defaultBranch))
        whenever(
            mockedAzureDevopsClient.createPullRequest(
                repo,
                "6.0",
                CreatePullRequest(
                    "refs/heads/$branch",
                    defaultBranch,
                    "Merge $branch into $defaultBranch",
                    "PR created by RET using `ret pr create --no-prompt`.",
                ),
            ),
        ).thenReturn(PullRequestCreated(createdPullRequestId))

        val exitCode = commandLine.execute("-r", repo, "--no-prompt", branch)
        Assertions.assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).println("$AZURE_DEVOPS_BASE_URL/organization/projectId/_git/$repo/pullrequest/$createdPullRequestId")
    }

    @Test
    fun `create should handle if PR is already created with --no-prompt`() {
        val branch = "feature/my-branch"
        val repo = "generic-project"
        val defaultBranch = "defaultBranch"

        whenever(mockedAzureDevopsClient.getRepositoryById(repo)).thenReturn(Repository(repo, defaultBranch))
        whenever(mockedAzureDevopsClient.createPullRequest(anyString(), anyString(), anyOrNull()))
            .thenThrow(ClientWebApplicationException(Response.Status.CONFLICT))

        val exitCode = commandLine.execute("-r", repo, "--no-prompt", branch)
        Assertions.assertThat(exitCode).isEqualTo(1)
        verify(outputHandler).error("A pull request for this branch already exists!")
    }

    @Test
    fun `create should handle if current branch is the same as the default branch with --no-prompt`() {
        val branch = "defaultBranch"
        val repo = "generic-project"
        val defaultBranch = "defaultBranch"

        whenever(mockedAzureDevopsClient.getRepositoryById(repo)).thenReturn(Repository(repo, defaultBranch))

        val exitCode = commandLine.execute("-r", repo, "--no-prompt", branch)
        Assertions.assertThat(exitCode).isEqualTo(2)
        verify(outputHandler).error("Could not create PR. Source branch is the same as the default branch.")
    }

    @Test
    fun `create should handle if no git context available with --no-prompt`() {
        val repo = "generic-project"

        val exitCode = commandLine.execute("-r", repo, "--no-prompt")
        Assertions.assertThat(exitCode).isEqualTo(2)
        verify(outputHandler).error("Could not determine branch from context. Please provide the branch.")
    }
}
