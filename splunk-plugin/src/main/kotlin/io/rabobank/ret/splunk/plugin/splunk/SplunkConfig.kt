package io.rabobank.ret.splunk.plugin.splunk

import io.rabobank.ret.configuration.BasePluginConfig
import io.rabobank.ret.configuration.ConfigurationProperty
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class SplunkConfig : BasePluginConfig() {
    val baseUrl: String? by lazy { config[BASE_URL] }
    val app: String? by lazy { config[APP] }
    val indexes: List<String> by lazy { config.get<String?>(INDEXES)?.run { split(",").map { it.trim() } }.orEmpty() }
    val searchField: String? by lazy { config[SEARCH_FIELD] }

    override fun keysToMigrate(): List<Pair<String, String>> =
        listOf(
            SPLUNK_BASE_URL to BASE_URL,
            SPLUNK_APP to APP,
        )

    override fun properties() = listOf(
        ConfigurationProperty(BASE_URL, "Enter the Splunk base URL", required = true),
        ConfigurationProperty(APP, "Enter your Splunk app name", required = true),
        ConfigurationProperty(
            INDEXES,
            "Enter your Splunk index, if more than one, separate by comma. E.g. my_index_a, my_index_b",
            required = true,
        ),
        //Optional answers from here on onwards:
        ConfigurationProperty(
            SEARCH_FIELD,
            "Optional: Enter the field of the unique identifier. E.g. system_name, application_name or cf_app_name.\n" +
                "This is handy in case you have one big index where all different sorts of applications/systems log to. E.g. my_awesome_microservice or my_linux_server_1",
        ),
    )

    private companion object {
        private const val SPLUNK_BASE_URL = "splunk_base_url"
        private const val SPLUNK_APP = "splunk_app"
        private const val BASE_URL = "base_url"
        private const val APP = "app"
        private const val INDEXES = "indexes"
        private const val SEARCH_FIELD = "search_field"
    }
}
