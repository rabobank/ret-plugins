package io.rabobank.ret.splunk.plugin

import io.rabobank.ret.RetContext
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.splunk.plugin.splunk.SplunkData
import io.rabobank.ret.splunk.plugin.splunk.SplunkDataItem
import io.rabobank.ret.util.BrowserUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import java.util.stream.Stream

private const val SPLUNK_BASE_URL = "splunk.base.url"

class SplunkSearchCommandTest {

    private lateinit var mockedBrowserUtils: BrowserUtils
    private lateinit var commandLine: CommandLine
    private lateinit var mockedRetContext: RetContext
    private lateinit var mockedSplunkData: SplunkData

    @BeforeEach
    fun before() {
        mockedBrowserUtils = mock()
        mockedRetContext = mock()
        mockedSplunkData = mock()

        val splunkCommand = SplunkSearchCommand(
            mockedBrowserUtils,
            mockedSplunkData,
            mockedRetContext,
        )

        splunkCommand.splunkBaseUrl = SPLUNK_BASE_URL
        splunkCommand.contextAwareness = ContextAwareness()

        commandLine = CommandLine(splunkCommand)
    }

    @ParameterizedTest
    @CsvSource("--index,--app", "-i,-a")
    fun `should open Splunk Dashboard with index and app provided`(indexFlag: String, appFlag: String) {
        val index = "splunk-index"
        val appName = "my-application"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+index%3D$index+cf_app_name%3D$appName"

        val exitCode = commandLine.execute(indexFlag, index, appFlag, appName)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with only index provided`() {
        val index = "splunk-index"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+index%3D$index"

        val exitCode = commandLine.execute("-i", index)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with index and app provided plus query part`() {
        val index = "splunk-index"
        val appName = "my-application"
        val queryPart = "loglevel=INFO"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+index%3D$index+cf_app_name%3D$appName+loglevel%3DINFO"

        val exitCode = commandLine.execute("--index", index, "--app", appName, queryPart)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard without query when Splunk is not context aware`() {
        val expectedURL = SPLUNK_BASE_URL

        val exitCode = commandLine.execute()
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with extra query part filled in`() {
        val queryPart1 = "loglevel=INFO origin != gorouter"
        val queryPart2 = "cf_space_name=my-space"

        val expectedURL =
            "$SPLUNK_BASE_URL?q=search+loglevel%3DINFO+origin+%21%3D+gorouter+cf_space_name%3Dmy-space"

        val exitCode = commandLine.execute(queryPart1, queryPart2)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with app name from execution context and corresponding index`() {
        val index = "splunk-index"
        val appName = "my-application"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+index%3D$index+cf_app_name%3D$appName"

        whenever(mockedRetContext.gitRepository).thenReturn(appName)

        whenever(mockedSplunkData.getAllByAppName(appName)).thenReturn(
            listOf(
                SplunkDataItem(index, "org", "space", appName),
            ),
        )

        val exitCode = commandLine.execute()
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should open Splunk Dashboard with app name provided and corresponding index`() {
        val appName = "my-application"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+index%3Dmy-index+cf_app_name%3D$appName"

        whenever(mockedSplunkData.getAllByAppName(appName)).thenReturn(
            listOf(
                SplunkDataItem("my-index", "org", "space", appName),
            ),
        )

        val exitCode = commandLine.execute("--app", appName)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @MethodSource("splunkData")
    fun `should get first index alphabetically, and prefer ending with '_p'`(
        splunkDataItems: List<SplunkDataItem>,
        expectedIndex: String,
    ) {
        val appName = "app"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+index%3D$expectedIndex+cf_app_name%3D$appName"

        whenever(mockedSplunkData.getAllByAppName(appName)).thenReturn(splunkDataItems)

        val exitCode = commandLine.execute("--app", appName)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `should not include index of no index is found`() {
        val appName = "app"

        val expectedURL = "$SPLUNK_BASE_URL?q=search+cf_app_name%3D$appName"

        whenever(mockedSplunkData.getAllByAppName(appName)).thenReturn(emptyList())

        val exitCode = commandLine.execute("--app", appName)
        assertThat(exitCode).isEqualTo(0)

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    companion object {
        @JvmStatic
        fun splunkData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    listOf(
                        SplunkDataItem("index-2", "org", "space", "app"),
                        SplunkDataItem("index-1", "org", "space", "app"),
                    ),
                    "index-1",
                ),
                Arguments.of(
                    listOf(
                        SplunkDataItem("def", "org", "space", "app"),
                        SplunkDataItem("abc", "org", "space", "app"),
                        SplunkDataItem("ghi_p", "org", "space", "app"),
                    ),
                    "ghi_p",
                ),
                Arguments.of(
                    listOf(
                        SplunkDataItem("def", "org", "space", "app"),
                        SplunkDataItem("abc", "org", "space", "app"),
                        SplunkDataItem("ghi", "org", "space", "app"),
                    ),
                    "abc",
                ),
            )
        }
    }
}
