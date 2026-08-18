package me.novavpn.vpn.engine

import android.content.Context
import me.novavpn.vpn.data.AuthApi
import me.novavpn.vpn.data.SessionStore
import me.novavpn.vpn.engine.proxy.ProxyVpnService
import me.novavpn.vpn.engine.wireguard.WireGuardEngine
import me.novavpn.vpn.model.TunnelProtocol
import me.novavpn.vpn.model.ConnectionPreferences
import hev.htproxy.TProxyService
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.novavpn.vpn.model.VpnRegion

class TunnelManager(context: Context) {
    private val app = context.applicationContext
    private val api = AuthApi()
    private val sessions = SessionStore(app)
    private val wireGuard = WireGuardEngine(app, api, sessions)
    private var active: TunnelProtocol? = null
    private var preferences = ConnectionPreferences()

    fun setPreferences(value: ConnectionPreferences) { preferences = value }

    suspend fun connect(protocol: TunnelProtocol, region: VpnRegion): Result<Unit> = when (protocol) {
        TunnelProtocol.WIREGUARD -> wireGuard.connect(region, preferences).onSuccess { active = protocol }
        TunnelProtocol.SOCKS5 -> runCatching {
            val token = sessions.accessToken() ?: error("missing_session")
            val credentials = api.proxyCredentials(token)
            ProxyVpnService.start(app, region.proxyHost, 1080, credentials.username, credentials.password)
            active = protocol
        }
        else -> Result.failure(UnsupportedOperationException("${protocol.title} integration is not enabled in this build"))
    }

    suspend fun traffic(): Pair<Long, Long> = when (active) {
        TunnelProtocol.WIREGUARD -> wireGuard.traffic()
        TunnelProtocol.SOCKS5 -> runCatching { TProxyService.TProxyGetStats().let { (it.getOrNull(3) ?: 0L) to (it.getOrNull(1) ?: 0L) } }.getOrDefault(0L to 0L)
        else -> 0L to 0L
    }

    suspend fun publicIp(): String = withContext(Dispatchers.IO) {
        runCatching { URL("https://api.ipify.org").readText().trim() }.getOrDefault("Protegida")
    }

    suspend fun disconnect(): Result<Unit> {
        val result = when (active) {
            TunnelProtocol.WIREGUARD -> wireGuard.disconnect()
            TunnelProtocol.SOCKS5 -> runCatching { ProxyVpnService.stop(app) }
            else -> Result.success(Unit)
        }
        active = null
        return result
    }
}
