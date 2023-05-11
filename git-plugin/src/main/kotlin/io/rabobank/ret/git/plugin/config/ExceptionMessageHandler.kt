package io.rabobank.ret.git.plugin.config

import io.quarkus.logging.Log
import io.rabobank.ret.git.plugin.output.OutputHandler
import picocli.CommandLine

class ExceptionMessageHandler(private val outputHandler: OutputHandler) : CommandLine.IExecutionExceptionHandler {
    override fun handleExecutionException(exception: Exception, commandLine: CommandLine, parseResult: CommandLine.ParseResult): Int {
        val exitCode = when (exception) {
            is IllegalArgumentException -> {
                Log.warn("Input error occurred", exception)

                exception.message?.let { outputHandler.error(it) }
                outputHandler.error(commandLine.usageMessage)
                commandLine.commandSpec.exitCodeOnInvalidInput()
            }

            else -> {
                exception.message?.let { outputHandler.error(it) }
                Log.error("An error occurred", exception)
                commandLine.commandSpec.exitCodeOnExecutionException()
            }
        }

        return exitCode
    }
}
