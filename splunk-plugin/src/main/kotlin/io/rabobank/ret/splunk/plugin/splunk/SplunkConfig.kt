package io.rabobank.ret.splunk.plugin.splunk

import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.ConfigurationProperty
import io.rabobank.ret.configuration.RetConfig
import jakarta.enterprise.context.ApplicationScoped

private const val SPLUNK_BASE_URL = "splunk_base_url"
private const val SPLUNK_APP = "splunk_app"

@ApplicationScoped
class SplunkConfig(retConfig: RetConfig) : Configurable {
    val splunkBaseUrl: String = retConfig[SPLUNK_BASE_URL].orEmpty()

    val splunkApp: String = retConfig[SPLUNK_APP].orEmpty()

    override fun properties(): List<ConfigurationProperty> {
        return listOf(
            ConfigurationProperty(SPLUNK_BASE_URL, "Enter the Splunk base URL"),
            ConfigurationProperty(SPLUNK_APP, "Enter your Splunk app name"),
        )
    }
}
