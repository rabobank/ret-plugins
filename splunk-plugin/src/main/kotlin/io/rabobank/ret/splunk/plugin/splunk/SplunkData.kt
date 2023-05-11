package io.rabobank.ret.splunk.plugin.splunk

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.InputStream

@ApplicationScoped
class SplunkData(
    @ConfigProperty(name = "splunk.data.path")
    private val dataPath: String,
) {

    private val dataItems by lazy {
        val inputStream = javaClass.getResourceAsStream(dataPath)
        checkNotNull(inputStream) { "Cannot find Splunk data at location $dataPath" }
        readCsv(inputStream)
    }

    fun getAllByAppName(appName: String): List<SplunkDataItem> {
        return dataItems.filter { it.cfAppName == appName }
    }

    fun getAll() = dataItems

    private fun readCsv(inputStream: InputStream): List<SplunkDataItem> {
        val reader = inputStream.bufferedReader()
        return reader.lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .map { line ->
                val (index, appName, organisation, space) = line.split(',').map { it.removeSurrounding("\"", "\"") }
                SplunkDataItem(index, organisation, space, appName)
            }.toList()
    }
}

data class SplunkDataItem(
    val index: String,
    val cfOrganisation: String,
    val cfSpaceName: String,
    val cfAppName: String,
)
