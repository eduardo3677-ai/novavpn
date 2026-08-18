package me.novavpn.vpn

import me.novavpn.vpn.data.ServiceCatalog
import me.novavpn.vpn.domain.server.ServerSorter
import me.novavpn.vpn.model.ServerSort
import me.novavpn.vpn.model.ServerStats
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerSorterTest {
    @Test fun nearest_uses_measured_latency() {
        val stats = mapOf("jp" to ServerStats(latencyMs = 1), "mx" to ServerStats(latencyMs = 200))
        assertEquals("jp", ServerSorter.sort(ServiceCatalog.regions, stats, ServerSort.NEAREST).first().code)
    }
    @Test fun farthest_reverses_latency_order() {
        val sorted = ServerSorter.sort(ServiceCatalog.regions, emptyMap(), ServerSort.FARTHEST)
        assertEquals("au", sorted.first().code)
    }
}
