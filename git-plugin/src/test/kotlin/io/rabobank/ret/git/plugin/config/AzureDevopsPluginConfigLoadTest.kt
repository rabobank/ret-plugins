package io.rabobank.ret.git.plugin.config

import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.RetConfig
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsPluginConfig
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.inject.Instance
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AzureDevopsPluginConfigLoadTest {

    @Test
    fun shouldLoadConfiguration() {
        val osUtils = mock<OsUtils>()
        val configurables = mock<Instance<Configurable>>()
        whenever(osUtils.getHomeDirectory()).thenReturn("src/test/resources")

        val pluginConfig = AzureDevopsPluginConfig(RetConfig(osUtils, configurables, "1.0.0"))

        Assertions.assertThat(pluginConfig.email).isEqualTo("manks@live.com")
        Assertions.assertThat(pluginConfig.pat).isEqualTo("this_is_a_pat")
        Assertions.assertThat(pluginConfig.projectId).isEqualTo("this_is_the_project")
        Assertions.assertThat(pluginConfig.organization).isEqualTo("my-organization")
    }

    @Test
    fun shouldLoadCorrectlyWithEmptyConfiguration() {
        val osUtils = mock<OsUtils>()
        val configurables = mock<Instance<Configurable>>()
        whenever(osUtils.getHomeDirectory()).thenReturn("src/test/resources/nonexisting")

        val pluginConfig = AzureDevopsPluginConfig(RetConfig(osUtils, configurables, "1.0.0"))

        Assertions.assertThat(pluginConfig.email).isEmpty()
        Assertions.assertThat(pluginConfig.pat).isEmpty()
        Assertions.assertThat(pluginConfig.projectId).isEmpty()
        Assertions.assertThat(pluginConfig.organization).isEmpty()
    }
}