package io.rabobank.ret.git.plugin.azure

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection
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
)

data class PullRequestCreated(
    @JsonProperty("pullRequestId") val pullRequestId: String,
)

@RegisterForReflection
data class CreatePullRequest(
    @JsonProperty("sourceRefName") val sourceRefName: String,
    @JsonProperty("targetRefName") val targetRefName: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
)

data class Reviewer(@JsonProperty("uniqueName") val uniqueName: String)

data class Repository(
    @JsonProperty("name") val name: String,
    @JsonProperty("defaultBranch") val defaultBranch: String?,
)

data class Branch(@JsonProperty("name") val name: String) {
    val shortName = name.substringAfter("refs/heads/")
}

data class Pipeline(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("folder") val folder: String,
) {
    val cleanedFolder = folder.removePrefix("\\")
    val uniqueName = "$cleanedFolder\\$name"
}

data class PipelineRun(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("createdDate") val createdDate: ZonedDateTime,
    @JsonProperty("state") val state: PipelineRunState,
    @JsonProperty("result") val result: PipelineRunResult?,
)

enum class PipelineRunState {
    @JsonProperty("canceling")
    CANCELING,

    @JsonProperty("completed")
    COMPLETED,

    @JsonProperty("inProgress")
    IN_PROGRESS,

    @JsonProperty("unknown")
    UNKNOWN,
}

enum class PipelineRunResult {
    @JsonProperty("canceled")
    CANCELED,

    @JsonProperty("failed")
    FAILED,

    @JsonProperty("succeeded")
    SUCCEEDED,

    @JsonProperty("unknown")
    UNKNOWN,
}
