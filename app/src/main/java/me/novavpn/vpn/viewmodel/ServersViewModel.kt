package me.novavpn.vpn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.novavpn.vpn.data.ServiceCatalog
import me.novavpn.vpn.data.network.LatencyMonitor
import me.novavpn.vpn.domain.server.ServerSorter
import me.novavpn.vpn.model.ServerSort
import me.novavpn.vpn.model.ServerStats
import me.novavpn.vpn.model.VpnRegion

data class ServersUiState(
    val regions: List<VpnRegion> = ServiceCatalog.regions,
    val stats: Map<String, ServerStats> = emptyMap(),
    val sort: ServerSort = ServerSort.NEAREST,
    val continent: String = "Todos",
    val refreshing: Boolean = false,
)

class ServersViewModel(private val monitor: LatencyMonitor = LatencyMonitor()) : ViewModel() {
    private val _state = MutableStateFlow(ServersUiState())
    val state: StateFlow<ServersUiState> = _state.asStateFlow()

    init { refreshLatencies() }

    fun setSort(sort: ServerSort) { update(sort = sort) }
    fun setContinent(continent: String) { update(continent = continent) }

    fun refreshLatencies() {
        if (_state.value.refreshing) return
        viewModelScope.launch {
            _state.value = _state.value.copy(refreshing = true)
            val measured = ServiceCatalog.regions.map { region ->
                async { region.code to monitor.measure(region.proxyHost, 1080) }
            }.awaitAll().toMap()
            _state.value = _state.value.copy(stats = measured, refreshing = false)
            update()
        }
    }

    private fun update(sort: ServerSort = _state.value.sort, continent: String = _state.value.continent) {
        val filtered = ServiceCatalog.regions.filter { continent == "Todos" || it.continent == continent }
        _state.value = _state.value.copy(
            regions = ServerSorter.sort(filtered, _state.value.stats, sort),
            sort = sort,
            continent = continent,
        )
    }
}
