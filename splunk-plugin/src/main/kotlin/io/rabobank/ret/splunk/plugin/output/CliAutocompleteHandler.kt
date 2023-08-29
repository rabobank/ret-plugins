package io.rabobank.ret.splunk.plugin.output

import io.rabobank.ret.RetConsole

class CliAutocompleteHandler(private val retConsole: RetConsole) : OutputHandler {
    override fun listIndexes(indexes: List<String>) {
        indexes.forEach(retConsole::out)
    }
}
