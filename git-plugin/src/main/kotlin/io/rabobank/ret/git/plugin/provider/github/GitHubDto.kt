package io.rabobank.ret.git.plugin.provider.github

import com.fasterxml.jackson.annotation.JsonProperty
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.GitDomainConvertible
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository
import io.rabobank.ret.git.plugin.provider.Branch as GenericBranch
import io.rabobank.ret.git.plugin.provider.PullRequest as GenericPullRequest
import io.rabobank.ret.git.plugin.provider.Repository as GenericRepository

data class Repository(
    @JsonProperty("url") val url: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("default_branch") val defaultBranch: String
) : GitDomainConvertible<GenericRepository> {
    override fun toGenericDomain() = GenericRepository(name, defaultBranch)
}

data class Branch(
    @JsonProperty("name") val name: String
) : GitDomainConvertible<GenericBranch> {
    override fun toGenericDomain(): Branch = GenericBranch(name)
}

data class PullRequestReference(
    @JsonProperty("repository_url") val repositoryUrl: String,
    @JsonProperty("number") val number: String, // TODO - id looks to be unique over whole GH, but can we use it for URLs?
    @JsonProperty("title") val title: String
) : GitDomainConvertible<GenericPullRequest> {
    override fun toGenericDomain(): PullRequest = GenericPullRequest(
        number,
        title,
        Repository(repositoryUrl.substringAfterLast("/"), null), // TODO - can we get the default-branch somehow?
        listOf() // TODO - how to resolve this? We cannot get reviewers without calling the API again per PR.
    )
}

data class PullRequestReferences(
    @JsonProperty("items") val items: List<PullRequestReference>
)