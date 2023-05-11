package io.rabobank.ret.splunk.plugin.output

import io.rabobank.ret.RetConsole
import io.rabobank.ret.splunk.plugin.splunk.SplunkDataItem

class CliOutputHandler(private val retConsole: RetConsole) : OutputHandler {

    override fun println(message: String) {
        retConsole.out(message)
    }

    override fun error(message: String) {
        retConsole.errorOut(message)
    }

    override fun listSplunkAppNames(list: List<SplunkDataItem>) {
        throw UnsupportedOperationException()
    }

    override fun listSplunkIndexes(list: List<SplunkDataItem>) {
        throw UnsupportedOperationException()
    }
}
