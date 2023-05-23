package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.RetContext
import io.rabobank.ret.commands.PluginInitializeCommand
import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.RetConfig
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkConfig
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.inject.Instance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import picocli.CommandLine.IFactory

private const val SPLUNK_URL = "splunk.base.url/en-GB/app/appName/search"

class SplunkEntryCommandTest {

    private lateinit var mockedBrowserUtils: BrowserUtils
    private lateinit var commandLine: CommandLine
    private lateinit var mockedRetContext: RetContext

    @BeforeEach
    fun before() {
        val configurables: Instance<Configurable> = mock()
        val retConfig = RetConfig(OsUtils(), configurables, "1.0.0")
        retConfig["splunk_base_url"] = "splunk.base.url"
        retConfig["splunk_app"] = "appName"

        mockedBrowserUtils = mock()
        mockedRetContext = mock()

        val splunkCommand = SplunkEntryCommand(
            mockedBrowserUtils,
            mockedRetContext,
            SplunkConfig(retConfig),
        )

        splunkCommand.contextAwareness = ContextAwareness()

        commandLine = CommandLine(
            splunkCommand,
            CustomInitializationFactory(),
        )
    }

    @ParameterizedTest
    @CsvSource("--index,--app", "-i,-a")
    fun `should open Splunk Dashboard with index and app provided`(indexFlag: String, appFlag: String) {
        val index = "splunk-index"
        val appName = "my-application"

        val expectedURL = "$SPLUNK_URL?q=search+index%3D$index+cf_app_name%3D$appName"

        val exitCode = commandLine.execute(indexFlag, index, appFlag, appName)
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
        val appName = "my-application"
        val queryPart = "loglevel=INFO"

        val expectedURL = "$SPLUNK_URL?q=search+index%3D$index+cf_app_name%3D$appName+loglevel%3DINFO"

        val exitCode = commandLine.execute("--index", index, "--app", appName, queryPart)
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
        val appName = "my-application"

        val expectedURL = "$SPLUNK_URL?q=search+cf_app_name%3D$appName"

        whenever(mockedRetContext.gitRepository).thenReturn(appName)

        val exitCode = commandLine.execute()
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with app name provided`() {
        val appName = "my-application"

        val expectedURL = "$SPLUNK_URL?q=search+cf_app_name%3D$appName"

        val exitCode = commandLine.execute("--app", appName)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }
}

class CustomInitializationFactory : IFactory {
    private val pluginInitializeCommand: PluginInitializeCommand = mock()
    override fun <K : Any?> create(cls: Class<K>?): K {
        return if (cls?.isInstance(pluginInitializeCommand) == true) {
            cls.cast(pluginInitializeCommand)
        } else {
            CommandLine.defaultFactory().create(cls)
        }
    }
}
