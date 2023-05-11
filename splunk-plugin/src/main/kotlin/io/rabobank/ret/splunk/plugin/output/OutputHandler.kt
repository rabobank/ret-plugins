package io.rabobank.ret.splunk.plugin.output

import io.rabobank.ret.splunk.plugin.splunk.SplunkDataItem

interface OutputHandler {
    fun println(message: String)
    fun error(message: String)
    fun listSplunkAppNames(list: List<SplunkDataItem>)
    fun listSplunkIndexes(list: List<SplunkDataItem>)
}
