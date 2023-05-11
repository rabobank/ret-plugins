package io.rabobank.ret.splunk.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.splunk.plugin.splunk.SplunkDataItem

class AlfredOutputHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) : OutputHandler {

    override fun error(message: String) {
        retConsole.out(objectMapper.writeValueAsString(Wrapper(listOf(Item("Error: $message", false)))))
    }

    override fun println(message: String) {
        retConsole.out(objectMapper.writeValueAsString(Wrapper(listOf(Item(message)))))
    }

    override fun listSplunkAppNames(list: List<SplunkDataItem>) {
        throw UnsupportedOperationException()
    }

    override fun listSplunkIndexes(list: List<SplunkDataItem>) {
        throw UnsupportedOperationException()
    }
}
