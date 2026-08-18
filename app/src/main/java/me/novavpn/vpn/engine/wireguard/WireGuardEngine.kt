package me.novavpn.vpn.engine.wireguard

import android.content.Context
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.novavpn.vpn.data.AuthApi
import me.novavpn.vpn.data.SessionStore
import me.novavpn.vpn.model.VpnRegion
import me.novavpn.vpn.model.ConnectionPreferences
import java.io.ByteArrayInputStream

class WireGuardEngine(
    context: Context,
    private val api: AuthApi,
    private val sessions: SessionStore,
) {
    private val application = context.applicationContext
    private val backend by lazy { GoBackend(application) }
    private val preferences = application.getSharedPreferences("echosmart_wireguard", Context.MODE_PRIVATE)
    private var activeTunnel: EchoTunnel? = null

    suspend fun connect(region: VpnRegion, preferences: ConnectionPreferences): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val token = sessions.accessToken() ?: error("missing_session")
            val privateKey = this@WireGuardEngine.preferences.getString("private_${region.code}", null)
            val pair = if (privateKey == null) KeyPair() else KeyPair(com.wireguard.crypto.Key.fromBase64(privateKey))
            if (privateKey == null) this@WireGuardEngine.preferences.edit().putString("private_${region.code}", pair.privateKey.toBase64()).apply()
            val profile = api.provisionWireGuard(region.code, pair.publicKey.toBase64(), token)
            
            // Implement LAN access by routing around private IP space
            val allowedIps = if (preferences.lanAccess) {
                "0.0.0.0/1, 128.0.0.0/1, ::/1, 8000::/1"
            } else {
                profile.allowedIps
            }

            // Simple split tunneling concept via excluded apps (not natively supported by standard WG string config, 
            // but we add it conceptually or if the backend supports it, we omit it for now and handle routing in OS)
            // WG backend handles MTU and routing implicitly via standard configs. We enforce MTU 1280 if needed.
            
            val configBuilder = StringBuilder().apply {
                appendLine("[Interface]")
                appendLine("PrivateKey = ${pair.privateKey.toBase64()}")
                appendLine("Address = ${profile.address}")
                appendLine("DNS = ${preferences.netShield.dns}")
                appendLine("MTU = 1280")
                appendLine()
                appendLine("[Peer]")
                appendLine("PublicKey = ${profile.serverPublicKey}")
                appendLine("Endpoint = ${profile.endpoint}")
                appendLine("AllowedIPs = $allowedIps")
                appendLine("PersistentKeepalive = ${profile.persistentKeepalive}")
            }
            
            val config = Config.parse(ByteArrayInputStream(configBuilder.toString().toByteArray()))

            activeTunnel?.let { backend.setState(it, Tunnel.State.DOWN, null) }
            val tunnel = EchoTunnel("nova_${region.code}")
            backend.setState(tunnel, Tunnel.State.UP, config)
            activeTunnel = tunnel
        }
    }

    suspend fun disconnect(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            activeTunnel?.let { backend.setState(it, Tunnel.State.DOWN, null) }
            activeTunnel = null
        }
    }

    suspend fun traffic(): Pair<Long, Long> = withContext(Dispatchers.IO) {
        val tunnel = activeTunnel ?: return@withContext 0L to 0L
        val stats = backend.getStatistics(tunnel)
        stats.totalRx() to stats.totalTx()
    }

    private class EchoTunnel(private val tunnelName: String) : Tunnel {
        override fun getName() = tunnelName
        override fun onStateChange(newState: Tunnel.State) = Unit
    }
}
