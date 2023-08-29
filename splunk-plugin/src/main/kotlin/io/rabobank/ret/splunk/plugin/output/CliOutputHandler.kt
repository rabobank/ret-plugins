package io.rabobank.ret.splunk.plugin.output

import io.rabobank.ret.RetConsole

class CliOutputHandler(private val retConsole: RetConsole) : OutputHandler {

    override fun println(message: String) {
        retConsole.out(message)
    }

    override fun error(message: String) {
        retConsole.errorOut(message)
    }

    override fun listIndexes(indexes: List<String>) {
        throw UnsupportedOperationException()
    }
}
