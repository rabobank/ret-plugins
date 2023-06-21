package io.rabobank.ret.git.plugin.provider.github

import com.fasterxml.jackson.annotation.JsonProperty
import io.rabobank.ret.git.plugin.provider.GitDomainConvertible
import io.rabobank.ret.git.plugin.provider.Repository as GenericRepository

data class Repository(
    @JsonProperty("url") val url: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("default_branch") val defaultBranch: String
) : GitDomainConvertible<GenericRepository> {
    override fun toGenericDomain() = GenericRepository(name, defaultBranch)
}