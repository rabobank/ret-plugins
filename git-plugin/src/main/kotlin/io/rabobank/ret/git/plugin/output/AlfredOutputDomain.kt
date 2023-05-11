package io.rabobank.ret.git.plugin.output

import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.git.plugin.azure.Branch
import io.rabobank.ret.git.plugin.azure.PullRequest
import io.rabobank.ret.git.plugin.azure.Repository

@RegisterForReflection
data class Wrapper(val items: List<Item>)

@RegisterForReflection
data class ItemIcon(val path: String)

@RegisterForReflection
data class Item(
    val title: String,
    val arg: String? = null,
    val subtitle: String = "",
    val icon: ItemIcon? = null,
    val valid: Boolean = true,
) {
    constructor(message: String) :
        this(
            title = message,
        )

    constructor(message: String, valid: Boolean) :
        this(
            title = message,
            valid = valid,
        )

    constructor(pr: PullRequest) :
        this(
            title = pr.title,
            subtitle = pr.repository.name,
            arg = pr.id,
            icon = ItemIcon("icons/pull_request.png"),
        )

    constructor(repo: Repository) :
        this(
            title = repo.name,
            arg = repo.name,
            icon = ItemIcon("icons/icon_repo.png"),
        )

    constructor(branch: Branch) :
        this(
            title = branch.shortName,
            arg = branch.shortName,
        )
}
