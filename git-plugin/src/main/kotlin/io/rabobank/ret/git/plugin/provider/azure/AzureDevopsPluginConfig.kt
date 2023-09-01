package io.rabobank.ret.git.plugin.provider.azure

import com.fasterxml.jackson.annotation.JsonProperty
import io.rabobank.ret.configuration.BasePluginConfig
import io.rabobank.ret.configuration.ConfigurationProperty
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class AzureDevopsPluginConfig : BasePluginConfig() {
    val config by lazy { convertTo<AzureDevopsConfig>() }

    override fun properties() = listOf(
        ConfigurationProperty(EMAIL, "Enter your Azure email address", required = true),
        ConfigurationProperty(PAT, "Enter your Azure Personal Access Token (PAT)", required = true),
        ConfigurationProperty(PROJECT, "Enter your Azure project", required = true),
        ConfigurationProperty(ORGANIZATION, "Enter your Azure organization", required = true),
    )

    /**
     * This will copy configuration from ret.config into plugins/git.json
     */
    override fun keysToMigrate(): List<Pair<String, String>> =
        listOf(
            EMAIL to EMAIL,
            PAT to PAT,
            PROJECT to PROJECT,
            ORGANIZATION to ORGANIZATION,
        )

    private companion object {
        private const val EMAIL = "azure_devops_email"
        private const val PAT = "azure_devops_pat"
        private const val PROJECT = "azure_devops_project"
        private const val ORGANIZATION = "azure_devops_organization"
    }
}

data class AzureDevopsConfig(
    @JsonProperty("azure_devops_email")
    val email: String?,
    @JsonProperty("azure_devops_pat")
    val pat: String?,
    @JsonProperty("azure_devops_project")
    val project: String?,
    @JsonProperty("azure_devops_organization")
    val organization: String?,
)
