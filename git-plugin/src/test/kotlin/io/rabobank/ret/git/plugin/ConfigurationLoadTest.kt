package io.rabobank.ret.git.plugin

import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.RetConfig
import io.rabobank.ret.git.plugin.config.PluginConfig
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.inject.Instance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConfigurationLoadTest {

    @Test
    fun shouldLoadConfiguration() {
        val osUtils: OsUtils = mock()
        val configurables: Instance<Configurable> = mock()
        whenever(osUtils.getHomeDirectory()).thenReturn("src/test/resources")

        val pluginConfig = PluginConfig(RetConfig(osUtils, configurables, "1.0.0"))

        assertThat(pluginConfig.email).isEqualTo("manks@live.com")
        assertThat(pluginConfig.pat).isEqualTo("this_is_a_pat")
        assertThat(pluginConfig.projectId).isEqualTo("this_is_the_project")
        assertThat(pluginConfig.organization).isEqualTo("my-organization")
    }

    @Test
    fun shouldLoadCorrectlyWithEmptyConfiguration() {
        val osUtils: OsUtils = mock()
        val configurables: Instance<Configurable> = mock()
        whenever(osUtils.getHomeDirectory()).thenReturn("src/test/resources/nonexisting")

        val pluginConfig = PluginConfig(RetConfig(osUtils, configurables, "1.0.0"))

        assertThat(pluginConfig.email).isEmpty()
        assertThat(pluginConfig.pat).isEmpty()
        assertThat(pluginConfig.projectId).isEmpty()
        assertThat(pluginConfig.organization).isEmpty()
    }
}
