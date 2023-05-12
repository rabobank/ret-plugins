package io.rabobank.ret.splunk.plugin.output

interface OutputHandler {
    fun println(message: String)
    fun error(message: String)
}
