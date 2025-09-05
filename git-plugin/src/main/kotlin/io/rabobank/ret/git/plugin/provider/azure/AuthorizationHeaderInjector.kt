package io.rabobank.ret.git.plugin.provider.azure

import jakarta.inject.Singleton
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.MultivaluedMap
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory
import java.util.Base64

@Singleton
class AuthorizationHeaderInjector(
    private val pluginConfig: AzureDevopsPluginConfig,
) : ClientHeadersFactory {
    override fun update(
        incomingHeaders: MultivaluedMap<String, String>,
        outgoingHeaders: MultivaluedMap<String, String>,
    ): MultivaluedMap<String, String> {
        val result: MultivaluedMap<String, String> = MultivaluedHashMap()
        val encodedPat = String(Base64.getEncoder().encode(":${pluginConfig.config.pat}".toByteArray()))
        result.add("Authorization", "Basic $encodedPat")
        return result
    }
}
