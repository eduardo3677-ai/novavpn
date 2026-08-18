package me.novavpn.vpn.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Ikev2VpnProfile
import android.net.VpnManager
import android.net.VpnService
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import me.novavpn.vpn.MainActivity
import me.novavpn.vpn.model.TunnelProtocol
import me.novavpn.vpn.model.VpnRegion
import java.security.cert.X509Certificate

interface TunnelEngine {
    val protocol: TunnelProtocol
    suspend fun prepare(region: VpnRegion): Result<Unit>
    suspend fun connect(region: VpnRegion): Result<Unit>
    suspend fun disconnect(): Result<Unit>
}

/**
 * Integration contract for the production engines. WireGuard/OpenVPN profiles are
 * already exported by the Azure deployment and will be imported by their native
 * adapters in the next iteration. SOCKS/HTTPS use a VpnService + tun-to-proxy bridge.
 */
class EngineRegistry {
    val supported = TunnelProtocol.entries.toSet()
}

@RequiresApi(Build.VERSION_CODES.R)
class Ikev2ProfileProvisioner(private val context: Context) {
    fun provision(
        server: String,
        identity: String,
        username: String,
        password: String,
        rootCa: X509Certificate,
    ): Intent? {
        val profile = Ikev2VpnProfile.Builder(server, identity)
            .setAuthUsernamePassword(username, password, rootCa)
            .setBypassable(false)
            .setMetered(false)
            .build()
        return context.getSystemService(VpnManager::class.java).provisionVpnProfile(profile)
    }
}

class EchoVpnService : VpnService() {
    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Conexión NovaVPN", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val launch = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(me.novavpn.vpn.R.drawable.nova_notification)
            .setContentTitle("NovaVPN protegido")
            .setContentText(intent?.getStringExtra(EXTRA_REGION) ?: "Túnel cifrado activo")
            .setContentIntent(launch)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    companion object {
        const val EXTRA_REGION = "region"
        private const val CHANNEL_ID = "echosmart_vpn"
        private const val NOTIFICATION_ID = 2108

        fun start(context: Context, region: VpnRegion) {
            val intent = Intent(context, EchoVpnService::class.java).putExtra(EXTRA_REGION, region.city)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, EchoVpnService::class.java))
        }
    }
}
