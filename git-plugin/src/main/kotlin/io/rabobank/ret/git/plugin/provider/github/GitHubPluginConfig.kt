package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.configuration.Configurable
import io.rabobank.ret.configuration.ConfigurationProperty
import io.rabobank.ret.configuration.RetConfig
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GitHubPluginConfig(retConfig: RetConfig) : Configurable {
    val username = retConfig[USERNAME].orEmpty()
    val pat = retConfig[PAT].orEmpty()
    val organization = retConfig[ORGANIZATION].orEmpty()

    override fun properties(): List<ConfigurationProperty> = listOf(
        ConfigurationProperty(USERNAME, "Enter your GitHub username"),
        ConfigurationProperty(PAT, "Enter your GitHub Personal Access Token (PAT)"),
        ConfigurationProperty(ORGANIZATION, "Enter your GitHub organization"),
    )

    private companion object {
        private const val USERNAME = "github_username"
        private const val PAT = "github_pat"
        private const val ORGANIZATION = "github_organization"
    }
}
