package io.rabobank.ret.git.plugin

import io.quarkus.test.junit.QuarkusTest
import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.RetContext
import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.RetConfig
import io.rabobank.ret.git.plugin.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.azure.AzureResponse
import io.rabobank.ret.git.plugin.azure.Branch
import io.rabobank.ret.git.plugin.azure.Pipeline
import io.rabobank.ret.git.plugin.azure.PipelineRun
import io.rabobank.ret.git.plugin.azure.PipelineRunResult
import io.rabobank.ret.git.plugin.azure.PipelineRunState
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository
import io.rabobank.ret.git.plugin.azure.Reviewer
import io.rabobank.ret.git.plugin.command.AutoCompleteCommand
import io.rabobank.ret.git.plugin.config.PluginConfig
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.inject.Instance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime

@QuarkusTest
class AutoCompleteCommandTest {
    private lateinit var commandLine: CommandLine
    private val azureDevopsClient = mock<AzureDevopsClient>()
    private val output = StringWriter()
    private val outputHandler = mock<OutputHandler>()
    private val mockedRetContext = mock<RetContext>()
    private lateinit var allMockedPullRequests: List<PullRequest>
    private lateinit var allMockedRepositories: List<Repository>

    @BeforeEach
    fun beforeEach() {
        val configurables = mock<Instance<Configurable>>()
        val retConfig = RetConfig(OsUtils(), configurables, "1.0.0")
        retConfig["azure_devops_email"] = "manks@live.com"
        retConfig["azure_devops_pat"] = "pat"
        retConfig["azure_devops_project"] = "projectId"
        retConfig["azure_devops_organization"] = "organization"

        val command = AutoCompleteCommand(
            azureDevopsClient,
            PluginConfig(retConfig),
            IntelliSearch(),
            outputHandler,
            mockedRetContext,
        )

        command.contextAwareness = ContextAwareness()
        commandLine = CommandLine(command)
        commandLine.out = PrintWriter(output)

        allMockedRepositories = listOf(
            Repository("admin-service", "refs/heads/master"),
            Repository("client-service", "refs/heads/master"),
            Repository("generic-project", "refs/heads/master"),
            Repository("open-source-tool", "refs/heads/master"),
        )
        whenever(azureDevopsClient.getAllRepositories()).thenReturn(AzureResponse.of(allMockedRepositories))
        whenever(
            azureDevopsClient.getAllRefs(
                "admin-service",
                "heads/",
            ),
        ).thenReturn(
            AzureResponse.of(
                Branch("refs/heads/feature/abc"),
                Branch("refs/heads/feature/ahum"),
                Branch("refs/heads/feature/def"),
            ),
        )
        allMockedPullRequests = listOf(
            PullRequest("1234",
                "PR Title",
                Repository("repo", "refs/heads/master"),
                listOf(Reviewer("manks@live.com"))),
            PullRequest("1235",
                "Add logo",
                Repository("generic-project", "refs/heads/master"),
                listOf(Reviewer("manks@live.com"))),
            PullRequest("1241", "NOJIRA: ahum", Repository("ret-engineering-tools", "refs/heads/master"), listOf()),
            PullRequest("1271", "NOJIRA: MANKS", Repository("ret-engineering-tools", "refs/heads/master"), listOf()),
            PullRequest("1272", "update admin-service", Repository("test", "refs/heads/master"), listOf()),
        )
        whenever(azureDevopsClient.getAllPullRequests()).thenReturn(AzureResponse.of(allMockedPullRequests))
    }

    @Test
    fun `should return all repository names in azdo project`() {
        val exitCode = commandLine.execute("git-repository")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listRepositories(
            allMockedRepositories,
        )
    }

    @ParameterizedTest
    @MethodSource("repositoryTest")
    fun `should return all repository names that matches the word`(word: String, repositories: List<Repository>) {
        val exitCode = commandLine.execute("git-repository", "--word=$word")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listRepositories(repositories)
    }

    @Test
    fun `should return nothing if nothing matches the word`() {
        val exitCode = commandLine.execute("git-repository", "--word=blu-engineering-tools")

        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).listRepositories(emptyList())
    }

    @Test
    fun `should return branches which match the word`() {
        verifyBranchesOutputted(
            commandLine.execute("git-branch", "--word=ahum", "--repository=admin-service"),
            listOf("refs/heads/feature/ahum"),
        )
    }

    @Test
    fun `should return all branches if no word is given`() {
        verifyBranchesOutputted(
            commandLine.execute("git-branch", "--repository=admin-service"),
            listOf(
                "refs/heads/feature/abc",
                "refs/heads/feature/ahum",
                "refs/heads/feature/def",
            ),
        )
    }

    @Test
    fun `should return all branches if an empty string as word is given`() {
        verifyBranchesOutputted(
            commandLine.execute("git-branch", "--word=", "--repository=admin-service"),
            listOf(
                "refs/heads/feature/abc",
                "refs/heads/feature/ahum",
                "refs/heads/feature/def",
            ),
        )
    }

    private fun verifyBranchesOutputted(exitCode: Int, branches: List<String>) {
        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).listBranches(
            branches.map { Branch(it) },
        )
    }

    @Test
    fun `should return no branches if nothing matches the word`() {
        verifyBranchesOutputted(
            commandLine.execute(
                "git-branch",
                "--word=123456",
                "--repository=admin-service",
            ),
            emptyList(),
        )
    }

    @Test
    fun `should fallback to repository from execution context if no repository is given`() {
        whenever(mockedRetContext.gitRepository).thenReturn("admin-service")

        verifyBranchesOutputted(
            commandLine.execute("git-branch", "--word=abc"),
            listOf(
                "refs/heads/feature/abc",
            ),
        )
    }

    @Test
    fun `should fallback to repository from execution context if an empty string as repository is given`() {
        whenever(mockedRetContext.gitRepository).thenReturn("admin-service")

        verifyBranchesOutputted(
            commandLine.execute("git-branch", "--word=abc", "--repository="),
            listOf("refs/heads/feature/abc"),
        )
    }

    @Test
    fun `should an error if no repository can be determined`() {
        val exitCode = commandLine.execute("git-branch", "--word=123456")

        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).error("No repository could be determined")
    }

    @Test
    fun `should return no branches if repository does not exist`() {
        whenever(
            azureDevopsClient.getAllRefs(
                "client-service-encryption",
                "heads/",
            ),
        ).thenReturn(
            AzureResponse.of(),
        )

        verifyBranchesOutputted(
            commandLine.execute("git-branch", "--word=123456", "--repository=client-service-encryption"),
            emptyList(),
        )
    }

    @Test
    fun azureDevopsReturnsPullRequestsAutocomplete() {
        verifyPullRequestsOutputted(setOf("1235"), "git-pullrequest", "--word=logo")
    }

    @Test
    fun azureDevopsReturnsPullRequestsAutocompleteIntelligentlyOnFirstLetters() {
        verifyPullRequestsOutputted(setOf("1241", "1271"), "git-pullrequest", "--word=ret")
    }

    @Test
    fun azureDevopsReturnsPullRequestsAutocompleteIntelligentlyOnPartialWords() {
        verifyPullRequestsOutputted(setOf("1241", "1271"), "git-pullrequest", "--word=retengt")
    }

    @Test
    fun azureDevopsReturnsPullRequestsFilterRepoOnContextAware() {
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        verifyPullRequestsOutputted(setOf("1235"), "git-pullrequest")
    }

    @Test
    fun azureDevopsReturnsPullRequestsFilterRepoOnFlagIgnoresContextAware() {
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        verifyPullRequestsOutputted(setOf("1241", "1271"), "git-pullrequest", "-r=ret-engineering-tools")
    }

    @Test
    fun noResultsReturned() {
        whenever(azureDevopsClient.getAllPullRequests()).thenReturn(
            AzureResponse.of(),
        )

        verifyPullRequestsOutputted(emptySet(), "git-pullrequest")
    }

    @Test
    fun azureDevopsReturnsPullRequests() {
        verifyPullRequestsOutputted(allMockedPullRequests.map { it.id }.toSet(), "git-pullrequest")
    }

    @ParameterizedTest
    @ValueSource(strings = ["-n", "--not-reviewed"])
    fun azureDevopsReturnsPullRequestsNotReviewed(flag: String) {
        verifyPullRequestsOutputted(setOf("1241", "1271", "1272"), "git-pullrequest", flag)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--ignore-context-aware", "-ica"])
    fun azureDevopsReturnsPullRequestsFilterRepoIgnoreContextAware(flag: String) {
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        verifyPullRequestsOutputted(allMockedPullRequests.map { it.id }.toSet(), flag, "git-pullrequest")
    }

    @Test
    fun azureDevopsReturnsPullRequestsAutocompleteIntelligentlyOnPartialWords2() {
        verifyPullRequestsOutputted(setOf("1272"), "git-pullrequest", "--word=upda")
    }

    @ParameterizedTest
    @ValueSource(strings = ["-r=ret-engineering-tools", "--repository=ret-engineering-tools"])
    fun azureDevopsReturnsPullRequestsFilterRepoOnFlag(flag: String) {
        verifyPullRequestsOutputted(setOf("1241", "1271"), "git-pullrequest", flag)
    }

    @Test
    fun `should autocomplete pipelines (name, folder) on word`() {
        whenever(azureDevopsClient.getAllPipelines()).thenReturn(
            AzureResponse.of(
                Pipeline(1, "admin-service deployment", "\\blabla"),
                Pipeline(2, "blabla", "\\admin-service"),
                Pipeline(3, "blabla", "\\blabla"),
            ),
        )

        val exitCode = commandLine.execute("git-pipeline", "-w", "as")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelines(
            argThat {
                this.containsAll(
                    listOf(
                        Pipeline(1, "admin-service deployment", "\\blabla"),
                        Pipeline(2, "blabla", "\\admin-service"),
                    ),
                ) && !this.contains(Pipeline(3, "blabla", "\\blabla"))
            },
        )
    }

    @Test
    fun `should autocomplete pipelines on folder and unique name`() {
        whenever(azureDevopsClient.getAllPipelines()).thenReturn(
            AzureResponse.of(
                Pipeline(1, "blabla", "\\admin-service"),
            ),
        )

        val exitCode = commandLine.execute("git-pipeline", "-w", "admin-service\\bla")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelines(
            listOf(
                Pipeline(1, "blabla", "\\admin-service"),
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("pipelineRunTest")
    fun `should autocomplete pipeline-runs on id, name, state and result`(
        autocompletionWord: String,
        expectedOutcome: List<PipelineRun>,
    ) {
        val pipelineId = "123456"
        whenever(azureDevopsClient.getPipelineRuns(pipelineId)).thenReturn(
            AzureResponse.of(
                PipelineRun(123, "name", staticCreatedDate, PipelineRunState.COMPLETED, PipelineRunResult.CANCELED),
                PipelineRun(
                    456,
                    "name 2",
                    staticCreatedDate,
                    PipelineRunState.IN_PROGRESS,
                    PipelineRunResult.UNKNOWN,
                ),
                PipelineRun(
                    789,
                    "name 3",
                    staticCreatedDate,
                    PipelineRunState.COMPLETED,
                    PipelineRunResult.SUCCEEDED,
                ),
            ),
        )

        val exitCode = commandLine.execute("git-pipeline-run", "--pipeline-id", pipelineId, "-w", autocompletionWord)
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelineRuns(expectedOutcome)
    }

    @Test
    fun `should autocomplete pipeline-runs using the pipeline folder and name as well as id`() {
        val pipeline = Pipeline(123456, "pipeline_name", "\\folder")
        val expectedResponse = PipelineRun(123,
            "name",
            staticCreatedDate,
            PipelineRunState.COMPLETED,
            PipelineRunResult.CANCELED)
        whenever(azureDevopsClient.getAllPipelines()).thenReturn(
            AzureResponse.of(
                pipeline,
            ),
        )
        whenever(azureDevopsClient.getPipelineRuns(pipeline.id.toString()))
            .thenReturn(AzureResponse.of(expectedResponse))

        val exitCode = commandLine.execute("git-pipeline-run", "--pipeline-id", "folder\\pipeline_name")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelineRuns(listOf(expectedResponse))
    }

    private fun verifyPullRequestsOutputted(pullRequestIds: Set<String>, vararg args: String) {
        val exitCode = commandLine.execute(*args)

        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).listPRs(
            allMockedPullRequests.filter { pullRequestIds.contains(it.id) },
        )
    }

    companion object {
        private val staticCreatedDate = ZonedDateTime.parse("1993-04-20T09:51:15.372293+01:00")

        @JvmStatic
        fun repositoryTest() = listOf(
            Arguments.of("as", listOf(Repository("admin-service", "refs/heads/master"))),
            Arguments.of("admin-service", listOf(Repository("admin-service", "refs/heads/master"))),
            Arguments.of(
                "service",
                listOf(
                    Repository("admin-service", "refs/heads/master"),
                    Repository("client-service", "refs/heads/master")
                ),
            ),
        )

        @JvmStatic
        fun pipelineRunTest() = listOf(
            Arguments.of(
                "123",
                listOf(
                    PipelineRun(123, "name", staticCreatedDate, PipelineRunState.COMPLETED, PipelineRunResult.CANCELED),
                ),
            ),
            Arguments.of(
                "name 3",
                listOf(
                    PipelineRun(
                        789,
                        "name 3",
                        staticCreatedDate,
                        PipelineRunState.COMPLETED,
                        PipelineRunResult.SUCCEEDED,
                    ),
                ),
            ),
            Arguments.of(
                "compl",
                listOf(
                    PipelineRun(123, "name", staticCreatedDate, PipelineRunState.COMPLETED, PipelineRunResult.CANCELED),
                    PipelineRun(
                        789,
                        "name 3",
                        staticCreatedDate,
                        PipelineRunState.COMPLETED,
                        PipelineRunResult.SUCCEEDED,
                    ),
                ),
            ),
            Arguments.of(
                "succee",
                listOf(
                    PipelineRun(
                        789,
                        "name 3",
                        staticCreatedDate,
                        PipelineRunState.COMPLETED,
                        PipelineRunResult.SUCCEEDED,
                    ),
                ),
            ),
            Arguments.of("non-existing", emptyList<PipelineRun>()),
        )
    }
}
