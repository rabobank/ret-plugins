package io.rabobank.ret.git.plugin.config

import io.quarkus.picocli.runtime.PicocliCommandLineFactory
import io.rabobank.ret.git.plugin.output.OutputHandler
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import picocli.CommandLine

@ApplicationScoped
class CommandLineConfiguration {
    @Produces
    fun customCommandLine(
        factory: PicocliCommandLineFactory,
        outputHandler: OutputHandler,
    ): CommandLine =
        factory
            .create()
            .setExecutionExceptionHandler(ExceptionMessageHandler(outputHandler))
}
