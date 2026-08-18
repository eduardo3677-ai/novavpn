package me.novavpn.vpn.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.novavpn.vpn.model.ConnectionPreferences
import me.novavpn.vpn.model.NetShieldMode
import me.novavpn.vpn.model.TunnelProtocol

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = application.getSharedPreferences("nova_preferences", 0)
    private val _preferences = MutableStateFlow(load())
    val preferences: StateFlow<ConnectionPreferences> = _preferences.asStateFlow()

    fun setProtocol(value: TunnelProtocol) = update(_preferences.value.copy(defaultProtocol = value))
    fun setAutoConnect(value: Boolean) = update(_preferences.value.copy(autoConnect = value))
    fun setKillSwitch(value: Boolean) = update(_preferences.value.copy(killSwitch = value))
    fun setLanAccess(value: Boolean) = update(_preferences.value.copy(lanAccess = value))
    fun setSplitTunneling(value: Boolean) = update(_preferences.value.copy(splitTunneling = value))
    fun setTcpFallback(value: Boolean) = update(_preferences.value.copy(useTcpFallback = value))
    fun setNetShield(value: NetShieldMode) = update(_preferences.value.copy(netShield = value))
    fun setVpnAccelerator(value: Boolean) = update(_preferences.value.copy(vpnAccelerator = value))

    private fun update(value: ConnectionPreferences) {
        _preferences.value = value
        store.edit()
            .putString("protocol", value.defaultProtocol.name)
            .putBoolean("auto", value.autoConnect)
            .putBoolean("kill", value.killSwitch)
            .putBoolean("lan", value.lanAccess)
            .putBoolean("split", value.splitTunneling)
            .putBoolean("tcp", value.useTcpFallback)
            .putString("netShield", value.netShield.name)
            .putBoolean("accelerator", value.vpnAccelerator)
            .apply()
    }

    private fun load() = ConnectionPreferences(
        defaultProtocol = runCatching { TunnelProtocol.valueOf(store.getString("protocol", null).orEmpty()) }.getOrDefault(TunnelProtocol.WIREGUARD),
        autoConnect = store.getBoolean("auto", true),
        killSwitch = store.getBoolean("kill", true),
        lanAccess = store.getBoolean("lan", false),
        splitTunneling = store.getBoolean("split", false),
        useTcpFallback = store.getBoolean("tcp", true),
        netShield = runCatching { NetShieldMode.valueOf(store.getString("netShield", null).orEmpty()) }.getOrDefault(NetShieldMode.MALWARE_ADS),
        vpnAccelerator = store.getBoolean("accelerator", true),
    )
}
