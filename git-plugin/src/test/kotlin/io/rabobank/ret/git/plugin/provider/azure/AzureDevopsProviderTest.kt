package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.PullRequest as GenericPullRequest
import io.rabobank.ret.git.plugin.provider.Repository as GenericRepository
import io.rabobank.ret.git.plugin.provider.Reviewer as GenericReviewer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AzureDevopsProviderTest {

    private val azureDevopsClient = mock<AzureDevopsClient>()
    private val pluginConfig = mock<AzureDevopsPluginConfig>()
    private val urlFactory = mock<AzureDevopsUrlFactory>()

    private val azureDevopsProvider = AzureDevopsProvider(azureDevopsClient, pluginConfig, urlFactory)

    @Test
    fun `pull requests can be filtered by whether they are reviewed by the user`() {
        whenever(pluginConfig.email).thenReturn("manks@live.com")
        val pr1 = PullRequest("1", "title", Repository("repo", "master"), listOf())
        val pr2 = pr1.copy(id = "2", reviewers = listOf(Reviewer("manks@live.com")))
        val pr3 = pr1.copy(id = "3", reviewers = listOf(Reviewer("other-manks@live.com")))
        whenever(azureDevopsClient.getAllPullRequests()).thenReturn(
            AzureResponse.of(pr1, pr2, pr3)
        )

        val pullRequests = azureDevopsProvider.getPullRequestsNotReviewedByUser()

        assertThat(pullRequests)
            .containsExactlyInAnyOrder(
                GenericPullRequest("1", "title", GenericRepository("repo", "master"), listOf()),
                GenericPullRequest("3", "title", GenericRepository("repo", "master"), listOf(GenericReviewer("other-manks@live.com"))))
    }

    @Test
    fun `pull requests can be created correctly`() {
        whenever(azureDevopsClient.createPullRequest(anyString(), anyString(), any())).thenReturn(PullRequestCreated("123"))

        azureDevopsProvider.createPullRequest("repo", "source", "target", "title", "desc")

        verify(azureDevopsClient).createPullRequest("repo", "6.0", CreatePullRequest("source", "target", "title", "desc"))
    }

}