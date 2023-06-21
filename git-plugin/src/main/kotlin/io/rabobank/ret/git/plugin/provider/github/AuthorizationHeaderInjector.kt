package io.rabobank.ret.git.plugin.provider.github

import jakarta.inject.Singleton
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.MultivaluedMap
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory

@Singleton
class AuthorizationHeaderInjector(private val pluginConfig: GitHubPluginConfig) : ClientHeadersFactory {

    override fun update(
        incomingHeaders: MultivaluedMap<String, String>,
        outgoingHeaders: MultivaluedMap<String, String>,
    ): MultivaluedMap<String, String> {
        val result = MultivaluedHashMap<String, String>()
        result.add("Accept", "application/vnd.github+json")
        result.add("Authorization", "Bearer ${pluginConfig.pat}")
        return result
    }
}
