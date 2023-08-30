package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.configuration.BasePluginConfig
import io.rabobank.ret.configuration.ConfigurationProperty
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class AzureDevopsPluginConfig : BasePluginConfig() {
    val email: String? by lazy { config[EMAIL] }
    val pat: String? by lazy { config[PAT] }
    val projectId: String? by lazy { config[PROJECT] }
    val organization: String? by lazy { config[ORGANIZATION] }

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
