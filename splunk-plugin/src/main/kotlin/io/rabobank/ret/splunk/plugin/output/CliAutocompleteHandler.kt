package io.rabobank.ret.splunk.plugin.output

import io.rabobank.ret.RetConsole
import io.rabobank.ret.splunk.plugin.splunk.SplunkDataItem

class CliAutocompleteHandler(private val retConsole: RetConsole) : OutputHandler {

    override fun println(message: String) {
    }

    override fun error(message: String) {
    }

    override fun listSplunkAppNames(list: List<SplunkDataItem>) {
        list.map { it.cfAppName }.forEach(retConsole::out)
    }

    override fun listSplunkIndexes(list: List<SplunkDataItem>) {
        list.map { it.index }.forEach(retConsole::out)
    }
}
