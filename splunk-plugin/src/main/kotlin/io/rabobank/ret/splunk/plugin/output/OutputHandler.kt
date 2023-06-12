package io.rabobank.ret.splunk.plugin.output

interface OutputHandler {
    fun println(message: String) {
        // No-op
    }

    fun error(message: String) {
        // No-op
    }
}
