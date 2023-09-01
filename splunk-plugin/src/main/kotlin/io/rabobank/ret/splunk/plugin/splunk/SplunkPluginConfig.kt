package io.rabobank.ret.splunk.plugin.splunk

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.rabobank.ret.configuration.BasePluginConfig
import io.rabobank.ret.configuration.ConfigurationProperty
import io.rabobank.ret.util.CommaDelimitedToListDeserializer
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class SplunkPluginConfig : BasePluginConfig() {
    val config by lazy { convertTo<SplunkConfig>() }

    override fun keysToMigrate(): List<Pair<String, String>> =
        listOf(
            SPLUNK_BASE_URL to BASE_URL,
            SPLUNK_APP to APP,
        )

    override fun properties() = listOf(
        ConfigurationProperty(BASE_URL, "Enter the Splunk base URL (<base-url>/en-US/<app-name>)", required = true),
        ConfigurationProperty(APP, "Enter your Splunk app name (<base-url>/en-US/<app-name>)", required = true),
        ConfigurationProperty(
            INDEXES,
            "Enter your Splunk index(es) (comma separated). E.g. my_index_a, my_index_b",
            required = true,
        ),
        // Optional answers from here on onwards:
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

data class SplunkConfig(
    @JsonProperty("base_url")
    val baseUrl: String,
    val app: String,
    @JsonDeserialize(using = CommaDelimitedToListDeserializer::class)
    val indexes: List<String> = emptyList(),
    @JsonProperty("search_field")
    val searchField: String? = null,
    val projects: List<SplunkProject> = emptyList(),
)

data class SplunkProject(
    val name: String,
)
