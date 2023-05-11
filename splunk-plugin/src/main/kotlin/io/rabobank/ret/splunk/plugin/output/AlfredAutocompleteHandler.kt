package io.rabobank.ret.splunk.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.splunk.plugin.splunk.SplunkDataItem

class AlfredAutocompleteHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) :
    OutputHandler {
    override fun println(message: String) {
        throw UnsupportedOperationException()
    }

    override fun error(message: String) {
        throw UnsupportedOperationException()
    }

    override fun listSplunkAppNames(list: List<SplunkDataItem>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No Splunk apps found", valid = false)))
                    else -> Wrapper(list.map { Item(title = it.cfAppName, arg = it.cfAppName) })
                },
            ),
        )
    }

    override fun listSplunkIndexes(list: List<SplunkDataItem>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                when (list.size) {
                    0 -> Wrapper(listOf(Item("No Splunk indexes found", valid = false)))
                    else -> Wrapper(list.map { Item(title = it.index, arg = it.index) })
                },
            ),
        )
    }
}
