package io.rabobank.ret.git.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.RetContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class EnvironmentManager {
    @Produces
    @ApplicationScoped
    fun outputHandler(
        retContext: RetContext,
        retConsole: RetConsole,
        objectMapper: ObjectMapper,
    ): OutputHandler =
        when (retContext.environment) {
            "CLI" -> CliOutputHandler(retConsole)
            "ZSH_AUTOCOMPLETE" -> CliAutocompleteHandler(retConsole)
            "ALFRED" -> AlfredOutputHandler(retConsole, objectMapper)
            "ALFRED_AUTOCOMPLETE" -> AlfredAutocompleteHandler(retConsole, objectMapper)
            else -> CliOutputHandler(retConsole)
        }
}
