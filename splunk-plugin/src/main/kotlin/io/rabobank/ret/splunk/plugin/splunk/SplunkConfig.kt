package io.rabobank.ret.splunk.plugin.splunk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.Question
import io.rabobank.ret.util.OsUtils
import jakarta.enterprise.context.ApplicationScoped
import java.nio.file.Path

@ApplicationScoped
class SplunkConfig(osUtils: OsUtils, objectMapper: ObjectMapper) : Configurable {
    private val pluginConfig = Path.of(osUtils.getHomeDirectory(), ".ret", "plugins", "splunk-plugin.json")
    private val config by lazy {
        runCatching {
            objectMapper.readValue<Map<String, String>>(pluginConfig.toFile())
        }.getOrDefault(emptyMap())
    }

    val baseUrl by lazy { config[BASE_URL] }
    val app by lazy { config[APP] }
    val indexes by lazy { config[INDEXES]?.run { split(",").map { it.trim() } }.orEmpty() }
    val searchField by lazy { config[SEARCH_FIELD] }

    override fun prompts() = listOf(
        Question(BASE_URL, "Enter the Splunk base URL", required = true),
        Question(APP, "Enter your Splunk app name", required = true),
        Question(INDEXES, "Enter your Splunk index, if more than one, separate by comma. Ex: my_index_a, my_index_b", required = true),
        //Optional answers from here on onwards:
        Question(
            SEARCH_FIELD,
            "Optional: Enter the field of the unique identifier. Ex: system_name, application_name or cf_app_name.\n" +
                "This is handy in case you have one big index where all different sorts of applications/systems log to. Ex: my_awesome_microservice or my_linux_server_1",
        ),
    )

    private companion object {
        private const val BASE_URL = "base_url"
        private const val APP = "app"
        private const val INDEXES = "indexes"
        private const val SEARCH_FIELD = "search_field"
    }
}
