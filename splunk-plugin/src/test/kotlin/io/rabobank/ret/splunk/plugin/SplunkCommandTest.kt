package io.rabobank.ret.splunk.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.rabobank.ret.RetContext
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkPluginConfig
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.OsUtils
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IFactory
import java.nio.file.Files
import java.nio.file.Path

class SplunkCommandTest {
    private val mockedBrowserUtils = mock<BrowserUtils>()
    private val retFolder by lazy { Files.createDirectory(mockUserHomeDirectory.resolve(".ret")) }
    private val pluginsPath by lazy { Files.createDirectory(retFolder.resolve("plugins")) }
    private val pluginConfigFileName = "splunk.json"
    private val mockedOsUtils by lazy {
        mock<OsUtils> {
            whenever(it.getHomeDirectory()).thenReturn(mockUserHomeDirectory.toString())
            whenever(it.getPluginConfig("splunk")).thenReturn(pluginsPath.resolve(pluginConfigFileName))
        }
    }
    private val mockedRetContext = mock<RetContext>()
    private lateinit var commandLine: CommandLine

    @TempDir
    lateinit var mockUserHomeDirectory: Path

    @BeforeEach
    fun before() {
        val splunkConfig =
            mapOf(
                "base_url" to "splunk.base.url",
                "app" to "general",
            )
        val objectMapper = jacksonObjectMapper()
        objectMapper.writeValue(mockedOsUtils.getPluginConfig("splunk").toFile(), splunkConfig)

        val splunkCommand =
            SplunkCommand(
                mockedBrowserUtils,
                mockedRetContext,
                SplunkPluginConfig().apply {
                    pluginName = "splunk"
                    this.objectMapper = objectMapper
                    osUtils = mockedOsUtils
                    retConfig = mock()
                },
            )

        splunkCommand.contextAwareness = ContextAwareness()

        commandLine = CommandLine(splunkCommand, CustomInitializationFactory())
    }

    @AfterEach
    fun tearDown() {
        FileUtils.deleteQuietly(mockUserHomeDirectory.toFile())
    }

    @ParameterizedTest
    @CsvSource("--index,--project", "-i,-p")
    fun `should open Splunk Dashboard with index and app provided`(
        indexFlag: String,
        projectFlag: String,
    ) {
        val index = "splunk-index"
        val project = "my-application"

        val expectedURL = "$SPLUNK_URL?q=search+index%3D$index+project%3D$project"

        val exitCode = commandLine.execute(indexFlag, index, projectFlag, project)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with only index provided`() {
        val index = "splunk-index"

        val expectedURL = "$SPLUNK_URL?q=search+index%3D$index"

        val exitCode = commandLine.execute("-i", index)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with index and app provided plus query part`() {
        val index = "splunk-index"
        val project = "my-application"
        val queryPart = "loglevel=INFO"

        val expectedURL = "$SPLUNK_URL?q=search+index%3D$index+project%3D$project+loglevel%3DINFO"

        val exitCode = commandLine.execute("--index", index, "--project", project, queryPart)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard without query when Splunk is not context aware`() {
        val expectedURL = SPLUNK_URL

        val exitCode = commandLine.execute()
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with extra query part filled in`() {
        val queryPart1 = "loglevel=INFO origin != gorouter"
        val queryPart2 = "cf_space_name=my-space"

        val expectedURL =
            "$SPLUNK_URL?q=search+loglevel%3DINFO+origin+%21%3D+gorouter+cf_space_name%3Dmy-space"

        val exitCode = commandLine.execute(queryPart1, queryPart2)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with app name from execution context`() {
        val project = "my-application"

        val expectedURL = "$SPLUNK_URL?q=search+project%3D$project"

        whenever(mockedRetContext.gitRepository).thenReturn(project)

        val exitCode = commandLine.execute()
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with app name provided`() {
        val project = "my-application"

        val expectedURL = "$SPLUNK_URL?q=search+project%3D$project"

        val exitCode = commandLine.execute("--project", project)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    private companion object {
        private const val SPLUNK_URL = "splunk.base.url/en-US/app/general/search"
    }
}

class CustomInitializationFactory : IFactory {
    override fun <K : Any> create(cls: Class<K>): K =
        if (cls.isAnnotationPresent(Command::class.java)) {
            mock(cls)
        } else {
            CommandLine.defaultFactory().create(cls)
        }
}
