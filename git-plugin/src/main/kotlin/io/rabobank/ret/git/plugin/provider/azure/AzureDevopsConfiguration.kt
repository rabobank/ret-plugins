package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.config.PluginConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.RestClientBuilder
import java.net.URI

@ApplicationScoped
class AzureDevopsConfiguration(
    private val pluginConfig: PluginConfig,
    @ConfigProperty(name = "azure.devops.baseUrl") private val azureDevopsBaseUrl: String,
) {

    @Produces
    fun azureDevopsRestClient(): AzureDevopsClient =
        RestClientBuilder.newBuilder()
            .baseUrl(
                UriBuilder.fromUri(URI.create(azureDevopsBaseUrl))
                    .path(pluginConfig.organization)
                    .path(pluginConfig.projectId)
                    .path("_apis")
                    .build()
                    .toURL(),
            )
            .build(AzureDevopsClient::class.java)
}
