package io.rabobank.ret.git.plugin.config

import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.ConfigurationProperty
import io.rabobank.ret.configuration.RetConfig
import jakarta.enterprise.context.ApplicationScoped

private const val EMAIL = "azure_devops_email"

private const val PAT = "azure_devops_pat"

private const val PROJECT = "azure_devops_project"

private const val ORGANIZATION = "azure_devops_organization"
@ApplicationScoped
class PluginConfig(retConfig: RetConfig) : Configurable {
    val email: String = retConfig[EMAIL].orEmpty()
    val pat: String = retConfig[PAT].orEmpty()
    val projectId: String = retConfig[PROJECT].orEmpty()
    val organization: String = retConfig[ORGANIZATION].orEmpty()

    override fun properties(): List<ConfigurationProperty> = listOf(
        ConfigurationProperty(EMAIL, "Enter your email-address"),
        ConfigurationProperty(PAT, "Enter your Azure Personal Access Token (PAT)"),
        ConfigurationProperty(PROJECT, "Enter your Azure project"),
        ConfigurationProperty(ORGANIZATION, "Enter your Azure organization"),
    )
}
