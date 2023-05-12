package io.rabobank.ret.splunk.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.logging.Log
import io.rabobank.ret.RetConsole
import io.rabobank.ret.splunk.plugin.output.Environment.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class EnvironmentManager {

    @Produces
    fun environment(@ConfigProperty(name = "ret.env", defaultValue = "CLI") retEnvironment: String): Environment = try {
        Log.debug("ret.env value is $retEnvironment")
        valueOf(retEnvironment)
    } catch (e: Exception) {
        CLI
    }

    @Produces
    @ApplicationScoped
    fun outputHandler(environment: Environment, retConsole: RetConsole, objectMapper: ObjectMapper): OutputHandler {
        Log.debug("Using output handler of type $environment")
        return when (environment) {
            CLI -> CliOutputHandler(retConsole)
            ZSH_AUTOCOMPLETE -> CliAutocompleteHandler()
            ALFRED -> AlfredOutputHandler(retConsole, objectMapper)
            ALFRED_AUTOCOMPLETE -> AlfredAutocompleteHandler()
        }
    }
}

enum class Environment {
    CLI,
    ZSH_AUTOCOMPLETE,
    ALFRED,
    ALFRED_AUTOCOMPLETE,
}
