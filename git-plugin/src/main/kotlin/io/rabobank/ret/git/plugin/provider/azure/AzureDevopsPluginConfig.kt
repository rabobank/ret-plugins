package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.configuration.ConfigurablePlugin
import io.rabobank.ret.configuration.ConfigurationProperty
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class AzureDevopsPluginConfig : ConfigurablePlugin() {
    val email by lazy { config[EMAIL].orEmpty() }
    val pat by lazy { config[PAT].orEmpty() }
    val projectId by lazy { config[PROJECT].orEmpty() }
    val organization by lazy { config[ORGANIZATION].orEmpty() }

    override fun properties() = listOf(
        ConfigurationProperty(EMAIL, "Enter your email address", required = true),
        ConfigurationProperty(PAT, "Enter your Azure Personal Access Token (PAT)", required = true),
        ConfigurationProperty(PROJECT, "Enter your Azure project", required = true),
        ConfigurationProperty(ORGANIZATION, "Enter your Azure organization", required = true),
    )

    private companion object {
        private const val EMAIL = "azure_devops_email"
        private const val PAT = "azure_devops_pat"
        private const val PROJECT = "azure_devops_project"
        private const val ORGANIZATION = "azure_devops_organization"
    }
}
