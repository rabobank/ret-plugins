package io.rabobank.ret.git.plugin.provider.github

import io.quarkus.rest.client.reactive.ClientQueryParam
import io.rabobank.ret.git.plugin.provider.utilities.LoggingFilter
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient


@RegisterRestClient
@RegisterClientHeaders(AuthorizationHeaderInjector::class)
@RegisterProvider(LoggingFilter::class)
interface GitHubClient {

    @GET
    @Path("/repos")
    @ClientHeaderParam(name = "Accept", value = ["application/vnd.github+json"])
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    @ClientQueryParam(name = "per_page", value = ["100"]) // TODO 100 is the max, replace this later with pagination
    fun getRepositories(): List<Repository>

}