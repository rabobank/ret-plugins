package io.rabobank.ret.git.plugin.command

import picocli.CommandLine.Command

@Command(
    name = "pr",
    description = ["Interact with pull requests"],
    subcommands = [PullRequestCreateCommand::class, PullRequestOpenCommand::class],
)
class PullRequestCommand
