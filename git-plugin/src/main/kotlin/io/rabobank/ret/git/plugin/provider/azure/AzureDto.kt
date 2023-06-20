package io.rabobank.ret.git.plugin.provider.azure

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.git.plugin.provider.GitDomain
import io.rabobank.ret.git.plugin.provider.GitDomainConvertible
import java.time.ZonedDateTime

data class AzureResponse<T>(
    @JsonProperty("count") val count: Int,
    @JsonProperty("value") val value: List<T>,
) {
    companion object {
        fun <T> of(vararg items: T): AzureResponse<T> = AzureResponse(items.size, items.asList())
        fun <T> of(items: List<T>): AzureResponse<T> = AzureResponse(items.size, items)
    }
}

data class PullRequest(
    @JsonProperty("pullRequestId") val id: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("repository") val repository: Repository,
    @JsonProperty("reviewers") val reviewers: List<Reviewer>,
) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.PullRequest> {
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.PullRequest(
        id,
        title,
        repository.toGenericDomain(),
        reviewers.map { it.toGenericDomain() }
    )
}

data class PullRequestCreated(
    @JsonProperty("pullRequestId") val pullRequestId: String,
) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.PullRequestCreated> {
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.PullRequestCreated(pullRequestId)
}

@RegisterForReflection
data class CreatePullRequest(
    @JsonProperty("sourceRefName") val sourceRefName: String,
    @JsonProperty("targetRefName") val targetRefName: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
)

fun io.rabobank.ret.git.plugin.provider.CreatePullRequest.fromGenericDomain() =
    CreatePullRequest(this.sourceRefName, this.targetRefName, this.title, this.description)

data class Reviewer(@JsonProperty("uniqueName") val uniqueName: String) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.Reviewer> {
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.Reviewer(uniqueName)
}

data class Repository(
    @JsonProperty("name") val name: String,
    @JsonProperty("defaultBranch") val defaultBranch: String?,
) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.Repository> {
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.Repository(name, defaultBranch)
}

data class Branch(@JsonProperty("name") val name: String) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.Branch> {
    val shortName = name.substringAfter("refs/heads/")
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.Branch(name)
}

data class Pipeline(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("folder") val folder: String,
) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.Pipeline> {
    val cleanedFolder = folder.removePrefix("\\")
    val uniqueName = "$cleanedFolder\\$name"
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.Pipeline(id, name, folder)
}

data class PipelineRun(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("createdDate") val createdDate: ZonedDateTime,
    @JsonProperty("state") val state: PipelineRunState,
    @JsonProperty("result") val result: PipelineRunResult?,
) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.PipelineRun> {
    override fun toGenericDomain() = io.rabobank.ret.git.plugin.provider.PipelineRun(id, name, createdDate, state.toGenericDomain(), result?.toGenericDomain())
}

enum class PipelineRunState(private val genericEquivalent: io.rabobank.ret.git.plugin.provider.PipelineRunState) :
    GitDomainConvertible<io.rabobank.ret.git.plugin.provider.PipelineRunState> {
    @JsonProperty("canceling")
    CANCELING(io.rabobank.ret.git.plugin.provider.PipelineRunState.CANCELING),

    @JsonProperty("completed")
    COMPLETED(io.rabobank.ret.git.plugin.provider.PipelineRunState.COMPLETED),

    @JsonProperty("inProgress")
    IN_PROGRESS(io.rabobank.ret.git.plugin.provider.PipelineRunState.IN_PROGRESS),

    @JsonProperty("unknown")
    UNKNOWN(io.rabobank.ret.git.plugin.provider.PipelineRunState.UNKNOWN);

    override fun toGenericDomain() = genericEquivalent
}

enum class PipelineRunResult(private val genericEquivalent: io.rabobank.ret.git.plugin.provider.PipelineRunResult) : GitDomainConvertible<io.rabobank.ret.git.plugin.provider.PipelineRunResult> {
    @JsonProperty("canceled")
    CANCELED(io.rabobank.ret.git.plugin.provider.PipelineRunResult.CANCELED),

    @JsonProperty("failed")
    FAILED(io.rabobank.ret.git.plugin.provider.PipelineRunResult.FAILED),

    @JsonProperty("succeeded")
    SUCCEEDED(io.rabobank.ret.git.plugin.provider.PipelineRunResult.SUCCEEDED),

    @JsonProperty("unknown")
    UNKNOWN(io.rabobank.ret.git.plugin.provider.PipelineRunResult.UNKNOWN);

    override fun toGenericDomain() = genericEquivalent
}

fun <T : GitDomain> List<GitDomainConvertible<T>>.toGenericDomain() = this.map { it.toGenericDomain() }
