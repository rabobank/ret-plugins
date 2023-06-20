package io.rabobank.ret.git.plugin.provider

import java.time.ZonedDateTime

interface GitDomain

interface GitDomainConvertible<T : GitDomain> {
    fun toGenericDomain(): T
}

data class PullRequest(
    val id: String,
    val title: String,
    val repository: Repository,
    val reviewers: List<Reviewer>,
) : GitDomain

data class PullRequestCreated(
    val pullRequestId: String,
) : GitDomain

data class Reviewer(val uniqueName: String) : GitDomain

data class Repository(
    val name: String,
    val defaultBranch: String?,
) : GitDomain

data class Branch(val name: String) : GitDomain {
    val shortName = name.substringAfter("refs/heads/")
}

data class Pipeline(
    val id: Int,
    val name: String,
    val folder: String,
) : GitDomain {
    val cleanedFolder = folder.removePrefix("\\")
    val uniqueName = "$cleanedFolder\\$name"
}

data class PipelineRun(
    val id: Int,
    val name: String,
    val createdDate: ZonedDateTime,
    val state: PipelineRunState,
    val result: PipelineRunResult?,
) : GitDomain

enum class PipelineRunState : GitDomain {
    CANCELING,
    COMPLETED,
    IN_PROGRESS,
    UNKNOWN,
}

enum class PipelineRunResult : GitDomain {
    CANCELED,
    FAILED,
    SUCCEEDED,
    UNKNOWN,
}