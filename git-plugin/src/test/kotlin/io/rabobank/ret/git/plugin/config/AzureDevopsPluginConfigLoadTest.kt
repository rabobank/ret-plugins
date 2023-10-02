package io.rabobank.ret.git.plugin.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsPluginConfig
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.whenever
import java.nio.file.Path
import kotlin.io.path.createDirectories

class AzureDevopsPluginConfigLoadTest {
    private val pluginConfigFileName = "git.json"
    private val pluginConfig =
        AzureDevopsPluginConfig()
            .apply {
                pluginName = "git"
                objectMapper = jacksonObjectMapper()
                osUtils = spy()
                retConfig = mock()
            }

    @TempDir
    lateinit var mockUserHomeDirectory: Path

    @BeforeEach
    fun setUp() {
        whenever(pluginConfig.osUtils.getHomeDirectory()).thenReturn(mockUserHomeDirectory.toString())
        pluginConfig.osUtils.getRetPluginsDirectory().createDirectories()

        val config =
            mapOf(
                "azure_devops_email" to "manks@live.com",
                "azure_devops_pat" to "this_is_a_pat",
                "azure_devops_project" to "this_is_the_project",
                "azure_devops_organization" to "my-organization",
            )
        pluginConfig.objectMapper.writeValue(
            pluginConfig.osUtils.getRetPluginsDirectory().resolve(pluginConfigFileName).toFile(),
            config,
        )
    }

    @AfterEach
    fun tearDown() {
        FileUtils.deleteQuietly(mockUserHomeDirectory.toFile())
    }

    @Test
    fun shouldLoadConfiguration() {
        assertThat(pluginConfig.config.email).isEqualTo("manks@live.com")
        assertThat(pluginConfig.config.pat).isEqualTo("this_is_a_pat")
        assertThat(pluginConfig.config.project).isEqualTo("this_is_the_project")
        assertThat(pluginConfig.config.organization).isEqualTo("my-organization")
    }

    @Test
    fun shouldLoadCorrectlyWithEmptyConfiguration() {
        whenever(pluginConfig.osUtils.getHomeDirectory()).thenReturn("$mockUserHomeDirectory/nonexisting")

        assertThat(pluginConfig.config.email).isEmpty()
        assertThat(pluginConfig.config.pat).isEmpty()
        assertThat(pluginConfig.config.project).isEmpty()
        assertThat(pluginConfig.config.organization).isEmpty()
    }
}
