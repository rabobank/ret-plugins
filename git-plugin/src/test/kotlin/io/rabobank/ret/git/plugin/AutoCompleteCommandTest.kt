package io.rabobank.ret.git.plugin

import io.quarkus.test.junit.QuarkusTest
import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.command.AutoCompleteCommand
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PipelineRunResult
import io.rabobank.ret.git.plugin.provider.PipelineRunState
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository
import io.rabobank.ret.git.plugin.provider.Reviewer
import io.rabobank.ret.picocli.mixin.ContextAwareness
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
    private val gitProvider = mock<GitProvider>()
    private val output = StringWriter()
    private val outputHandler = mock<OutputHandler>()
    private val mockedRetContext = mock<RetContext>()
    private lateinit var allMockedPullRequests: List<PullRequest>
    private lateinit var allMockedRepositories: List<Repository>

    @BeforeEach
    fun beforeEach() {
        val command =
            AutoCompleteCommand(
                gitProvider,
                IntelliSearch(),
                outputHandler,
                mockedRetContext,
            )

        command.contextAwareness = ContextAwareness()
        commandLine = CommandLine(command)
        commandLine.out = PrintWriter(output)

        mockedRepositories()
        mockedPullRequests()
    }

    private fun mockedRepositories() {
        allMockedRepositories =
            listOf(
                Repository("admin-service", "refs/heads/master"),
                Repository("client-service", "refs/heads/master"),
                Repository("generic-project", "refs/heads/master"),
                Repository("open-source-tool", "refs/heads/master"),
            )
        whenever(gitProvider.getAllRepositories()).thenReturn(allMockedRepositories)
        whenever(
            gitProvider.getAllRefs(
                "admin-service",
                "heads/",
            ),
        ).thenReturn(
            listOf(
                Branch("refs/heads/feature/abc", "feature/abc"),
                Branch("refs/heads/feature/ahum", "feature/ahum"),
                Branch("refs/heads/feature/def", "feature/def"),
            ),
        )
    }

    private fun mockedPullRequests() {
        allMockedPullRequests =
            listOf(
                PullRequest(
                    "1234",
                    "PR Title",
                    Repository("repo", "refs/heads/master"),
                    listOf(Reviewer("manks@live.com")),
                ),
                PullRequest(
                    "1235",
                    "Add logo",
                    Repository("generic-project", "refs/heads/master"),
                    listOf(Reviewer("manks@live.com")),
                ),
                PullRequest(
                    "1241",
                    "NOJIRA: ahum",
                    Repository("ret-engineering-tools", "refs/heads/master"),
                    listOf(),
                ),
                PullRequest(
                    "1271",
                    "NOJIRA: MANKS",
                    Repository("ret-engineering-tools", "refs/heads/master"),
                    listOf(),
                ),
                PullRequest("1272", "update admin-service", Repository("test", "refs/heads/master"), listOf()),
            )
        whenever(gitProvider.getAllPullRequests()).thenReturn(allMockedPullRequests)
        whenever(gitProvider.getPullRequestsNotReviewedByUser()).thenReturn(
            allMockedPullRequests.filter {
                it.reviewers.all { r -> r.uniqueName != "manks@live.com" }
            },
        )
    }

    @Test
    fun `should return all repository names in project`() {
        val exitCode = commandLine.execute("repository")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listRepositories(allMockedRepositories)
    }

    @ParameterizedTest
    @MethodSource("repositoryTest")
    fun `should return all repository names that matches the word`(
        word: String,
        repositories: List<Repository>,
    ) {
        val exitCode = commandLine.execute("repository", "--word=$word")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listRepositories(repositories)
    }

    @Test
    fun `should return nothing if nothing matches the word`() {
        val exitCode = commandLine.execute("repository", "--word=blu-engineering-tools")

        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).listRepositories(emptyList())
    }

    @Test
    fun `should return branches which match the word`() {
        verifyBranchesOutputted(
            commandLine.execute("branch", "--word=ahum", "--repository=admin-service"),
            listOf("refs/heads/feature/ahum"),
        )
    }

    @Test
    fun `should return all branches if no word is given`() {
        verifyBranchesOutputted(
            commandLine.execute("branch", "--repository=admin-service"),
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
            commandLine.execute("branch", "--word=", "--repository=admin-service"),
            listOf(
                "refs/heads/feature/abc",
                "refs/heads/feature/ahum",
                "refs/heads/feature/def",
            ),
        )
    }

    private fun verifyBranchesOutputted(
        exitCode: Int,
        branches: List<String>,
    ) {
        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).listBranches(
            branches.map { Branch(it, it.removePrefix("refs/heads/")) },
        )
    }

    @Test
    fun `should return no branches if nothing matches the word`() {
        verifyBranchesOutputted(
            commandLine.execute(
                "branch",
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
            commandLine.execute("branch", "--word=abc"),
            listOf(
                "refs/heads/feature/abc",
            ),
        )
    }

    @Test
    fun `should fallback to repository from execution context if an empty string as repository is given`() {
        whenever(mockedRetContext.gitRepository).thenReturn("admin-service")

        verifyBranchesOutputted(
            commandLine.execute("branch", "--word=abc", "--repository="),
            listOf("refs/heads/feature/abc"),
        )
    }

    @Test
    fun `should an error if no repository can be determined`() {
        val exitCode = commandLine.execute("branch", "--word=123456")

        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).error("No repository could be determined")
    }

    @Test
    fun `should return no branches if repository does not exist`() {
        whenever(
            gitProvider.getAllRefs(
                "client-service-encryption",
                "heads/",
            ),
        ).thenReturn(
            listOf(),
        )

        verifyBranchesOutputted(
            commandLine.execute("branch", "--word=123456", "--repository=client-service-encryption"),
            emptyList(),
        )
    }

    @Test
    fun gitProviderReturnsPullRequestsAutocomplete() {
        verifyPullRequestsOutputted(setOf("1235"), "pullrequest", "--word=logo")
    }

    @Test
    fun gitProviderReturnsPullRequestsAutocompleteIntelligentlyOnFirstLetters() {
        verifyPullRequestsOutputted(setOf("1241", "1271"), "pullrequest", "--word=ret")
    }

    @Test
    fun gitProviderReturnsPullRequestsAutocompleteIntelligentlyOnPartialWords() {
        verifyPullRequestsOutputted(setOf("1241", "1271"), "pullrequest", "--word=retengt")
    }

    @Test
    fun gitProviderReturnsPullRequestsFilterRepoOnContextAware() {
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        verifyPullRequestsOutputted(setOf("1235"), "pullrequest")
    }

    @Test
    fun gitProviderReturnsPullRequestsFilterRepoOnFlagIgnoresContextAware() {
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        verifyPullRequestsOutputted(setOf("1241", "1271"), "pullrequest", "-r=ret-engineering-tools")
    }

    @Test
    fun noResultsReturned() {
        whenever(gitProvider.getAllPullRequests()).thenReturn(
            listOf(),
        )

        verifyPullRequestsOutputted(emptySet(), "pullrequest")
    }

    @Test
    fun gitProviderReturnsPullRequests() {
        verifyPullRequestsOutputted(allMockedPullRequests.map { it.id }.toSet(), "pullrequest")
    }

    @ParameterizedTest
    @ValueSource(strings = ["-n", "--not-reviewed"])
    fun gitProviderReturnsPullRequestsNotReviewed(flag: String) {
        verifyPullRequestsOutputted(setOf("1241", "1271", "1272"), "pullrequest", flag)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--ignore-context-aware", "-ica"])
    fun gitProviderReturnsPullRequestsFilterRepoIgnoreContextAware(flag: String) {
        whenever(mockedRetContext.gitRepository).thenReturn("generic-project")

        verifyPullRequestsOutputted(allMockedPullRequests.map { it.id }.toSet(), flag, "pullrequest")
    }

    @Test
    fun gitProviderReturnsPullRequestsAutocompleteIntelligentlyOnPartialWords2() {
        verifyPullRequestsOutputted(setOf("1272"), "pullrequest", "--word=upda")
    }

    @ParameterizedTest
    @ValueSource(strings = ["-r=ret-engineering-tools", "--repository=ret-engineering-tools"])
    fun gitProviderReturnsPullRequestsFilterRepoOnFlag(flag: String) {
        verifyPullRequestsOutputted(setOf("1241", "1271"), "pullrequest", flag)
    }

    @Test
    fun `should autocomplete pipelines (name, folder) on word`() {
        whenever(gitProvider.getAllPipelines()).thenReturn(
            listOf(
                Pipeline(1, "admin-service deployment", "blabla", "blabla\\admin-service deployment"),
                Pipeline(2, "blabla", "admin-service", "admin-service\\blabla"),
                Pipeline(3, "blabla", "blabla", "blabla\\blabla"),
            ),
        )

        val exitCode = commandLine.execute("pipeline", "-w", "as")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelines(
            argThat {
                this.containsAll(
                    listOf(
                        Pipeline(1, "admin-service deployment", "blabla", "blabla\\admin-service deployment"),
                        Pipeline(2, "blabla", "admin-service", "admin-service\\blabla"),
                    ),
                ) && !this.contains(Pipeline(3, "blabla", "blabla", "blabla\\blabla"))
            },
        )
    }

    @Test
    fun `should autocomplete pipelines on folder and unique name`() {
        whenever(gitProvider.getAllPipelines()).thenReturn(
            listOf(
                Pipeline(1, "blabla", "admin-service", "admin-service\\blabla"),
            ),
        )

        val exitCode = commandLine.execute("pipeline", "-w", "admin-service\\bla")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelines(
            listOf(
                Pipeline(1, "blabla", "admin-service", "admin-service\\blabla"),
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
        whenever(gitProvider.getPipelineRuns(pipelineId)).thenReturn(
            listOf(
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

        val exitCode = commandLine.execute("pipeline-run", "--pipeline-id", pipelineId, "-w", autocompletionWord)
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelineRuns(expectedOutcome)
    }

    @Test
    fun `should autocomplete pipeline-runs using the pipeline folder and name as well as id`() {
        val pipeline = Pipeline(123456, "pipeline_name", "folder", "folder\\pipeline_name")
        val expectedResponse =
            PipelineRun(
                123,
                "name",
                staticCreatedDate,
                PipelineRunState.COMPLETED,
                PipelineRunResult.CANCELED,
            )
        whenever(gitProvider.getAllPipelines()).thenReturn(
            listOf(
                pipeline,
            ),
        )
        whenever(gitProvider.getPipelineRuns(pipeline.id.toString()))
            .thenReturn(listOf(expectedResponse))

        val exitCode = commandLine.execute("pipeline-run", "--pipeline-id", "folder\\pipeline_name")
        assertThat(exitCode).isEqualTo(0)

        verify(outputHandler).listPipelineRuns(listOf(expectedResponse))
    }

    private fun verifyPullRequestsOutputted(
        pullRequestIds: Set<String>,
        vararg args: String,
    ) {
        val exitCode = commandLine.execute(*args)

        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler).listPRs(
            allMockedPullRequests.filter { pullRequestIds.contains(it.id) },
        )
    }

    companion object {
        private val staticCreatedDate = ZonedDateTime.parse("1993-04-20T09:51:15.372293+01:00")

        @JvmStatic
        fun repositoryTest() =
            listOf(
                Arguments.of("as", listOf(Repository("admin-service", "refs/heads/master"))),
                Arguments.of("admin-service", listOf(Repository("admin-service", "refs/heads/master"))),
                Arguments.of(
                    "service",
                    listOf(
                        Repository("admin-service", "refs/heads/master"),
                        Repository("client-service", "refs/heads/master"),
                    ),
                ),
            )

        @JvmStatic
        fun pipelineRunTest() =
            listOf(
                Arguments.of(
                    "123",
                    listOf(
                        PipelineRun(
                            123,
                            "name",
                            staticCreatedDate,
                            PipelineRunState.COMPLETED,
                            PipelineRunResult.CANCELED,
                        ),
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
                        PipelineRun(
                            123,
                            "name",
                            staticCreatedDate,
                            PipelineRunState.COMPLETED,
                            PipelineRunResult.CANCELED,
                        ),
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
