package me.novavpn.vpn.model

import androidx.compose.ui.graphics.Color

enum class TunnelProtocol(
    val title: String,
    val subtitle: String,
    val accent: Color,
) {
    WIREGUARD("WireGuard", "UDP 51820 · máximo rendimiento", Color(0xFF54E8FF)),
    OPENVPN_UDP("OpenVPN UDP", "UDP 1194 · rápido y compatible", Color(0xFF6EE7B7)),
    OPENVPN_TCP("OpenVPN TCP", "TCP 443 · redes restrictivas", Color(0xFF38BDF8)),
    IKEV2("IKEv2", "IPsec · integración Android", Color(0xFFA78BFA)),
    SOCKS5("SOCKS5", "TCP 1080 · proxy autenticado", Color(0xFFFFB86B)),
    SOCKS4("SOCKS4", "TCP 1080 · compatibilidad clásica", Color(0xFFF59E0B)),
    HTTPS("HTTPS", "TLS 443 · proxy cifrado", Color(0xFFF472B6)),
}

data class VpnRegion(
    val code: String,
    val city: String,
    val country: String,
    val continent: String,
    val flag: String,
    val vpnHost: String,
    val proxyHost: String,
    val latencyMs: Int,
    val loadPercent: Int,
    val nodes: Int,
    val online: Boolean = true,
)

enum class AccountRole { CLIENT, ADMIN }

data class UserAccount(
    val name: String,
    val email: String,
    val role: AccountRole,
    val plan: String = "Quantum",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
)

enum class ConnectionPhase { DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING }

data class ConnectionUiState(
    val phase: ConnectionPhase = ConnectionPhase.DISCONNECTED,
    val region: VpnRegion,
    val protocol: TunnelProtocol = TunnelProtocol.WIREGUARD,
    val connectedSeconds: Long = 0,
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val publicIp: String = "—",
)

data class AdminMetrics(
    val registeredClients: Int = 1248,
    val activeSessions: Int = 386,
    val onlineNodes: Int = 16,
    val throughputGbps: Double = 3.89,
)

enum class ProxyType(val title: String, val port: Int) {
    SOCKS5("SOCKS5", 1080), HTTPS("HTTPS", 443), SOCKS4("SOCKS4", 1080)
}

data class ProxyEndpoint(
    val id: String,
    val region: VpnRegion,
    val type: ProxyType,
    val host: String,
    val authenticated: Boolean = true,
)

enum class ServerSort(val title: String) {
    NEAREST("Más cercanos"), FARTHEST("Más lejanos"), LOWEST_LOAD("Menor carga"), COUNTRY("País")
}

data class ServerStats(
    val latencyMs: Int? = null,
    val jitterMs: Int? = null,
    val available: Boolean = true,
    val measuredAt: Long = 0,
)

data class ConnectionPreferences(
    val defaultProtocol: TunnelProtocol = TunnelProtocol.WIREGUARD,
    val autoConnect: Boolean = true,
    val killSwitch: Boolean = true,
    val lanAccess: Boolean = false,
    val splitTunneling: Boolean = false,
    val useTcpFallback: Boolean = true,
    val netShield: NetShieldMode = NetShieldMode.MALWARE_ADS,
    val vpnAccelerator: Boolean = true,
)

enum class ConnectionProfile(val title: String, val description: String) {
    FASTEST("Conexión rápida", "Menor latencia disponible"),
    GAMING("Gaming", "Latencia baja y menor carga"),
    STREAMING("Streaming", "Equilibrio entre velocidad y estabilidad"),
    PRIVACY("Privacidad reforzada", "Kill switch y DNS antimalware"),
}

enum class NetShieldMode(val title: String, val dns: String) {
    OFF("Desactivado", "1.1.1.1"),
    MALWARE("Bloquear malware", "1.1.1.2"),
    MALWARE_ADS("Malware, anuncios y rastreadores", "1.1.1.3"),
}
