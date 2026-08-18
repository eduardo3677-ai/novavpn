package me.novavpn.vpn.domain.server

import me.novavpn.vpn.model.ServerSort
import me.novavpn.vpn.model.ServerStats
import me.novavpn.vpn.model.VpnRegion

object ServerSorter {
    fun sort(regions: List<VpnRegion>, stats: Map<String, ServerStats>, mode: ServerSort): List<VpnRegion> = when (mode) {
        ServerSort.NEAREST -> regions.sortedBy { stats[it.code]?.latencyMs ?: it.latencyMs }
        ServerSort.FARTHEST -> regions.sortedByDescending { stats[it.code]?.latencyMs ?: it.latencyMs }
        ServerSort.LOWEST_LOAD -> regions.sortedBy { it.loadPercent }
        ServerSort.COUNTRY -> regions.sortedWith(compareBy(VpnRegion::country, VpnRegion::city))
    }
}
