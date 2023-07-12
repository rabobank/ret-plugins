package io.rabobank.ret.git.plugin.provider

import java.time.ZonedDateTime

interface GitDomain

fun interface GitDomainConvertible<T : GitDomain> {
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

data class Branch(
    val name: String,
    val shortName: String
) : GitDomain

data class Pipeline(
    val id: Int,
    val name: String,

    /**
     * The container of where this pipeline belongs to.
     * For example the folder in Azure DevOps or the repository in GitHub.
     */
    val container: String,

    /**
     * A string which uniquely identifies this pipeline
     * For example for Azure DevOps: <folder>\<pipeline name> or for GitHub: <repo>/<pipeline name>
     */
    val uniqueName: String
) : GitDomain

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