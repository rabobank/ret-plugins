package io.rabobank.ret.git.plugin

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.RetContext
import io.rabobank.ret.commands.PluginInitializeCommand
import io.rabobank.ret.git.plugin.command.AutoCompleteCommand
import io.rabobank.ret.git.plugin.command.PipelineCommand
import io.rabobank.ret.git.plugin.command.PullRequestCommand
import io.rabobank.ret.git.plugin.command.RepositoryCommand
import picocli.CommandLine.Command

@TopCommand
@Command(
    name = "git",
    description = ["Plugin to interact with git provider"],
    subcommands = [
        PluginInitializeCommand::class,
        PullRequestCommand::class,
        PipelineCommand::class,
        RepositoryCommand::class,
        AutoCompleteCommand::class,
    ],
)
@RegisterForReflection(targets = [RetContext::class])
class GitEntryCommand : Runnable {
    override fun run() {
        println("test! ")
    }

}
