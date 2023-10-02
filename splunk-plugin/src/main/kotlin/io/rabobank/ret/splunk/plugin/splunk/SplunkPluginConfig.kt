package io.rabobank.ret.splunk.plugin.splunk

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.ConfigurationProperty
import io.rabobank.ret.util.CommaDelimitedToListDeserializer
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class SplunkPluginConfig : Configurable() {
    val config by lazy { convertTo<SplunkConfig>() }

    override fun keysToMigrate(): List<Pair<String, String>> =
        listOf(
            "splunk_base_url" to BASE_URL,
            "splunk_app" to APP,
        )

    override fun properties() =
        listOf(
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
                """
                Optional: Enter the field of the unique identifier. E.g. system_name, application_name or cf_app_name.
                This is handy in case you have one big index where all different sorts of applications/systems log to.
                E.g. my_awesome_microservice or my_linux_server_1
                """.trimIndent(),
            ),
        )

    private companion object {
        private const val BASE_URL = "base_url"
        private const val APP = "app"
        private const val INDEXES = "indexes"
        private const val SEARCH_FIELD = "search_field"
    }
}

data class SplunkConfig(
    @JsonProperty("base_url")
    val baseUrl: String?,
    val app: String?,
    @JsonDeserialize(using = CommaDelimitedToListDeserializer::class)
    val indexes: List<String> = emptyList(),
    @JsonProperty("search_field")
    val searchField: String? = null,
    val projects: List<SplunkProject> = emptyList(),
)

data class SplunkProject(
    val name: String,
)
