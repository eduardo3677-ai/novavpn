package me.novavpn.vpn.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.novavpn.vpn.model.ServerStats
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs

class LatencyMonitor {
    suspend fun measure(host: String, port: Int = 1080, attempts: Int = 4): ServerStats = withContext(Dispatchers.IO) {
        val address = runCatching { InetAddress.getAllByName(host).first() }.getOrNull()
            ?: return@withContext ServerStats(available = false, measuredAt = System.currentTimeMillis())
        val samples = buildList {
            repeat(attempts) {
                val started = System.nanoTime()
                val success = runCatching { Socket().use { socket -> socket.connect(InetSocketAddress(address, port), 2_500) } }.isSuccess
                if (success) add(((System.nanoTime() - started) / 1_000_000L).toInt())
            }
        }.sorted()
        if (samples.isEmpty()) ServerStats(available = false, measuredAt = System.currentTimeMillis())
        else {
            val median = samples[samples.size / 2]
            val jitter = samples.zipWithNext { a, b -> abs(a - b) }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
            ServerStats(median, jitter, true, System.currentTimeMillis())
        }
    }
}
