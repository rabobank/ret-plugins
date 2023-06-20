package io.rabobank.ret.git.plugin.provider

import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsProvider
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class GitProviderSelector(private val azureDevopsClient: AzureDevopsClient, private val azureDevopsUrlFactory: AzureDevopsUrlFactory) {

    @Produces
    fun gitProvider() = AzureDevopsProvider(azureDevopsClient, azureDevopsUrlFactory)
}