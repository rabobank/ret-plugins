package io.rabobank.ret.splunk.plugin.splunk

import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.ConfigurationProperty
import io.rabobank.ret.configuration.RetConfig
import jakarta.enterprise.context.ApplicationScoped

private const val CF_SPACE_NAME = "cf_space_names"

@ApplicationScoped
class SplunkConfig(retConfig: RetConfig) : Configurable {
    val cfSpaceNames: List<String> = retConfig[CF_SPACE_NAME]
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()

    override fun properties(): List<ConfigurationProperty> {
        return listOf(
            ConfigurationProperty(CF_SPACE_NAME, "Enter your CF Space Name(s), comma seperated"),
        )
    }
}
