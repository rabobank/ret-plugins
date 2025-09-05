package io.rabobank.ret.splunk.plugin.output

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class Wrapper(
    val items: List<Item>,
)

@RegisterForReflection
data class ItemIcon(
    val path: String,
)

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
}
