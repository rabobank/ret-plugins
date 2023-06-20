package io.rabobank.ret.git.plugin.provider.azure

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
@RegisterClientHeaders(AuthorizationHeaderInjector::class)
@RegisterProvider(LoggingFilter::class)
interface AzureDevopsClient {

    @GET
    @Path("/git/pullrequests")
    fun getAllPullRequests(): AzureResponse<PullRequest>

    @GET
    @Path("/git/pullrequests/{id}")
    fun getPullRequestById(
        @PathParam("id") id: String,
    ): PullRequest

    @POST
    @Path("/git/repositories/{repository}/pullrequests")
    fun createPullRequest(
        @PathParam("repository") repository: String,
        @QueryParam("api-version") apiVersion: String,
        createPullRequest: CreatePullRequest,
    ): PullRequestCreated

    @GET
    @Path("/git/repositories")
    fun getAllRepositories(): AzureResponse<Repository>

    @GET
    @Path("/git/repositories/{repository}")
    fun getRepositoryById(
        @PathParam("repository") repository: String,
    ): Repository

    @GET
    @Path("/git/repositories/{repository}/refs")
    fun getAllRefs(
        @PathParam("repository") repository: String,
        @QueryParam("filter") filter: String,
    ): AzureResponse<Branch>

    @GET
    @Path("/pipelines")
    fun getAllPipelines(): AzureResponse<Pipeline>

    @GET
    @Path("/pipelines/{pipelineId}/runs")
    fun getPipelineRuns(@PathParam("pipelineId") pipelineId: String): AzureResponse<PipelineRun>
}
