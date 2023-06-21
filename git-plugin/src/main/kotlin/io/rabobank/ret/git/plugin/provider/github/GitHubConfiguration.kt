package io.rabobank.ret.git.plugin.provider.github

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.RestClientBuilder
import java.net.URI

@ApplicationScoped
class GitHubConfiguration(
    @ConfigProperty(name = "github.api.baseUrl") private val githubApiBaseUrl: String,
) {

    @Produces
    fun gitHubRestClient(): GitHubClient =
        RestClientBuilder.newBuilder()
            .baseUrl(
                UriBuilder.fromUri(URI.create(githubApiBaseUrl))
                    .build()
                    .toURL(),
            )
            .build(GitHubClient::class.java)
}
