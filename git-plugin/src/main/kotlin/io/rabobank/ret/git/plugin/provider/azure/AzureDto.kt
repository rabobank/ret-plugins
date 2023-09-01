package io.rabobank.ret.git.plugin.provider.azure

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.git.plugin.provider.GitDomain
import io.rabobank.ret.git.plugin.provider.GitDomainConvertible
import java.time.ZonedDateTime
import io.rabobank.ret.git.plugin.provider.Branch as GenericBranch
import io.rabobank.ret.git.plugin.provider.Pipeline as GenericPipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun as GenericPipelineRun
import io.rabobank.ret.git.plugin.provider.PipelineRunResult as GenericPipelineRunResult
import io.rabobank.ret.git.plugin.provider.PipelineRunState as GenericPipelineRunState
import io.rabobank.ret.git.plugin.provider.PullRequest as GenericPullRequest
import io.rabobank.ret.git.plugin.provider.PullRequestCreated as GenericPullRequestCreated
import io.rabobank.ret.git.plugin.provider.Repository as GenericRepository
import io.rabobank.ret.git.plugin.provider.Reviewer as GenericReviewer

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
) : GitDomainConvertible<GenericPullRequest> {
    override fun toGenericDomain() = GenericPullRequest(
        id,
        title,
        repository.toGenericDomain(),
        reviewers.toGenericDomain(),
    )
}

data class PullRequestCreated(
    @JsonProperty("pullRequestId") val pullRequestId: String,
) : GitDomainConvertible<GenericPullRequestCreated> {
    override fun toGenericDomain() = GenericPullRequestCreated(pullRequestId)
}

@RegisterForReflection
data class CreatePullRequest(
    @JsonProperty("sourceRefName") val sourceRefName: String,
    @JsonProperty("targetRefName") val targetRefName: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
)

data class Reviewer(@JsonProperty("uniqueName") val uniqueName: String) : GitDomainConvertible<GenericReviewer> {
    override fun toGenericDomain() = GenericReviewer(uniqueName)
}

data class Repository(
    @JsonProperty("name") val name: String,
    @JsonProperty("defaultBranch") val defaultBranch: String?,
) : GitDomainConvertible<GenericRepository> {
    override fun toGenericDomain() = GenericRepository(name, defaultBranch)
}

data class Branch(@JsonProperty("name") val name: String) : GitDomainConvertible<GenericBranch> {
    override fun toGenericDomain(): GenericBranch {
        val shortName = name.substringAfter("refs/heads/")
        return GenericBranch(name, shortName)
    }
}

data class Pipeline(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("folder") val folder: String,
) : GitDomainConvertible<GenericPipeline> {
    override fun toGenericDomain(): GenericPipeline {
        val cleanedFolder = folder.removePrefix("\\")
        val uniqueName = "$cleanedFolder\\$name"
        return GenericPipeline(id, name, cleanedFolder, uniqueName)
    }
}

data class PipelineRun(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("createdDate") val createdDate: ZonedDateTime,
    @JsonProperty("state") val state: PipelineRunState,
    @JsonProperty("result") val result: PipelineRunResult?,
) : GitDomainConvertible<GenericPipelineRun> {
    override fun toGenericDomain() = GenericPipelineRun(
        id,
        name,
        createdDate,
        state.toGenericDomain(),
        result?.toGenericDomain(),
    )
}

enum class PipelineRunState(private val genericEquivalent: GenericPipelineRunState) :
    GitDomainConvertible<GenericPipelineRunState> {
    @JsonProperty("canceling")
    CANCELING(GenericPipelineRunState.CANCELING),

    @JsonProperty("completed")
    COMPLETED(GenericPipelineRunState.COMPLETED),

    @JsonProperty("inProgress")
    IN_PROGRESS(GenericPipelineRunState.IN_PROGRESS),

    @JsonProperty("unknown")
    UNKNOWN(GenericPipelineRunState.UNKNOWN),
    ;

    override fun toGenericDomain() = genericEquivalent
}

enum class PipelineRunResult(private val genericEquivalent: GenericPipelineRunResult) :
    GitDomainConvertible<GenericPipelineRunResult> {
    @JsonProperty("canceled")
    CANCELED(GenericPipelineRunResult.CANCELED),

    @JsonProperty("failed")
    FAILED(GenericPipelineRunResult.FAILED),

    @JsonProperty("succeeded")
    SUCCEEDED(GenericPipelineRunResult.SUCCEEDED),

    @JsonProperty("unknown")
    UNKNOWN(GenericPipelineRunResult.UNKNOWN),
    ;

    override fun toGenericDomain() = genericEquivalent
}

fun <T : GitDomain> Collection<GitDomainConvertible<T>>.toGenericDomain() = this.map { it.toGenericDomain() }
