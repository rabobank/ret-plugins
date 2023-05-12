package io.rabobank.ret.splunk.plugin

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.RetContext
import io.rabobank.ret.commands.PluginInitializeCommand
import picocli.CommandLine

@TopCommand
@CommandLine.Command(
    name = "splunk",
    description = ["Plugin to interact with Splunk"],
    subcommands = [
        PluginInitializeCommand::class,
        SplunkSearchCommand::class,
    ],
)
@RegisterForReflection(targets = [RetContext::class])
class SplunkEntryCommand
