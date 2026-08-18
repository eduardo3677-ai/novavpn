package me.novavpn.vpn.engine.proxy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import hev.htproxy.TProxyService
import me.novavpn.vpn.MainActivity
import me.novavpn.vpn.R
import java.io.File

class ProxyVpnService : VpnService() {
    private var tunnel: ParcelFileDescriptor? = null
    private var detachedFd: Int = -1

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "NovaVPN Proxy VPN", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopTunnel(); stopSelf(); return START_NOT_STICKY }
        val host = intent?.getStringExtra(EXTRA_HOST) ?: return START_NOT_STICKY
        val port = intent.getIntExtra(EXTRA_PORT, 1080)
        val username = intent.getStringExtra(EXTRA_USERNAME).orEmpty()
        val password = intent.getStringExtra(EXTRA_PASSWORD).orEmpty()
        startForeground(NOTIFICATION_ID, notification(host))
        runCatching { startTunnel(host, port, username, password) }.onFailure { stopSelf() }
        return START_STICKY
    }

    private fun startTunnel(host: String, port: Int, username: String, password: String) {
        stopTunnel()
        val builder = Builder()
            .setSession("NovaVPN SOCKS5 · $host")
            .setMtu(1500)
            .addAddress("198.18.0.1", 32)
            .addAddress("fc00::1", 128)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("2606:4700:4700::1111")
        builder.addDisallowedApplication(packageName)
        tunnel = builder.establish() ?: error("VPN permission not granted")
        val config = File(cacheDir, "hev-${System.currentTimeMillis()}.yml")
        config.writeText(
            """
            tunnel:
              mtu: 1500
              ipv4: 198.18.0.1
              ipv6: 'fc00::1'
              multi-queue: false
            socks5:
              address: '$host'
              port: $port
              udp: 'udp'
              pipeline: true
              username: '${yaml(username)}'
              password: '${yaml(password)}'
            mapdns:
              address: 198.18.0.2
              port: 53
              network: 100.64.0.0
              netmask: 255.192.0.0
              cache-size: 10000
            misc:
              task-stack-size: 86016
              tcp-buffer-size: 65536
              udp-recv-buffer-size: 4194304
              udp-copy-buffer-nums: 32
              connect-timeout: 5000
              tcp-read-write-timeout: 600000
              udp-read-write-timeout: 120000
              limit-nofile: 65535
              log-level: warn
            """.trimIndent()
        )
        detachedFd = tunnel!!.detachFd()
        check(TProxyService.TProxyStartService(config.absolutePath, detachedFd))
    }

    private fun stopTunnel() {
        if (TProxyService.TProxyIsRunning()) TProxyService.TProxyStopService()
        if (detachedFd >= 0) runCatching { ParcelFileDescriptor.adoptFd(detachedFd).close() }
        detachedFd = -1
        tunnel?.close(); tunnel = null
    }

    override fun onDestroy() { stopTunnel(); super.onDestroy() }

    private fun notification(host: String) = NotificationCompat.Builder(this, CHANNEL)
        .setSmallIcon(R.drawable.nova_notification)
        .setContentTitle("NovaVPN Proxy activo")
        .setContentText(host)
        .setOngoing(true)
        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
        .build()

    private fun yaml(value: String) = value.replace("'", "''")

    companion object {
        private const val CHANNEL = "echosmart_proxy"
        private const val NOTIFICATION_ID = 2110
        private const val ACTION_STOP = "me.novavpn.vpn.STOP_PROXY"
        const val EXTRA_HOST = "host"; const val EXTRA_PORT = "port"; const val EXTRA_USERNAME = "username"; const val EXTRA_PASSWORD = "password"
        fun start(context: Context, host: String, port: Int, username: String, password: String) {
            context.startForegroundService(Intent(context, ProxyVpnService::class.java).putExtra(EXTRA_HOST, host).putExtra(EXTRA_PORT, port).putExtra(EXTRA_USERNAME, username).putExtra(EXTRA_PASSWORD, password))
        }
        fun stop(context: Context) { context.startService(Intent(context, ProxyVpnService::class.java).setAction(ACTION_STOP)) }
    }
}
