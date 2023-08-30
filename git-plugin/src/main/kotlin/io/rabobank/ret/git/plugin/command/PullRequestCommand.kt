package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.util.Logged
import picocli.CommandLine.Command

@Command(
    name = "pr",
    description = ["Interact with pull requests"],
    subcommands = [PullRequestCreateCommand::class, PullRequestOpenCommand::class],
)
@Logged
class PullRequestCommand
