package io.rabobank.ret.git.plugin.provider.utilities

import io.quarkus.logging.Log
import jakarta.ws.rs.client.ClientRequestContext
import jakarta.ws.rs.client.ClientRequestFilter
import jakarta.ws.rs.ext.Provider

@Provider
class LoggingFilter : ClientRequestFilter {
    override fun filter(requestContext: ClientRequestContext) {
        Log.info("Calling endpoint: ${requestContext.method} ${requestContext.uri.toASCIIString()}")
    }
}
