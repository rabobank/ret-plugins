package io.rabobank.ret.splunk.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole

class AlfredAutocompleteHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) :
    OutputHandler {
    override fun println(message: String) {
        throw UnsupportedOperationException()
    }

    override fun error(message: String) {
        throw UnsupportedOperationException()
    }

    override fun listIndexes(indexes: List<String>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                if (indexes.isEmpty()) Wrapper(listOf(Item("No indexes found", valid = false)))
                else Wrapper(indexes.map { Item(it) })
            )
        )
    }
}
