package io.rabobank.ret.git.plugin.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsPluginConfig
import io.rabobank.ret.util.OsUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path

class AzureDevopsPluginConfigLoadTest {
    private val retFolder by lazy { Files.createDirectory(mockUserHomeDirectory.resolve(".ret")) }
    private val pluginsPath by lazy { Files.createDirectory(retFolder.resolve("plugins")) }
    private val pluginConfigFileName = "git.json"
    private val pluginConfig = AzureDevopsPluginConfig()
        .apply {
            pluginName = "git"
            objectMapper = jacksonObjectMapper()
        }

    @TempDir
    lateinit var mockUserHomeDirectory: Path

    @BeforeEach
    fun setUp() {
        Files.createFile(pluginsPath.resolve(pluginConfigFileName))

        val config = mapOf(
            "azure_devops_email" to "manks@live.com",
            "azure_devops_pat" to "this_is_a_pat",
            "azure_devops_project" to "this_is_the_project",
            "azure_devops_organization" to "my-organization",
        )
        pluginConfig.objectMapper.writeValue(pluginsPath.resolve(pluginConfigFileName).toFile(), config)
    }

    @Test
    fun shouldLoadConfiguration() {
        pluginConfig.osUtils = mock<OsUtils> {
            whenever(it.getHomeDirectory()).thenReturn(mockUserHomeDirectory.toString())
            whenever(it.getPluginConfig(pluginConfig.pluginName)).thenReturn(pluginsPath.resolve(pluginConfigFileName))
        }

        assertThat(pluginConfig.email).isEqualTo("manks@live.com")
        assertThat(pluginConfig.pat).isEqualTo("this_is_a_pat")
        assertThat(pluginConfig.projectId).isEqualTo("this_is_the_project")
        assertThat(pluginConfig.organization).isEqualTo("my-organization")
    }

    @Test
    fun shouldLoadCorrectlyWithEmptyConfiguration() {
        pluginConfig.osUtils = mock<OsUtils> {
            whenever(it.getHomeDirectory()).thenReturn("$mockUserHomeDirectory/nonexisting")
        }

        assertThat(pluginConfig.email).isNull()
        assertThat(pluginConfig.pat).isNull()
        assertThat(pluginConfig.projectId).isNull()
        assertThat(pluginConfig.organization).isNull()
    }
}
