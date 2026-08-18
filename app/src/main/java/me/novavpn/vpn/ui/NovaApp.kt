package me.novavpn.vpn.ui

import androidx.compose.animation.AnimatedContent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import me.novavpn.vpn.R
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.novavpn.vpn.feature.account.AccountScreen
import me.novavpn.vpn.feature.admin.AdminScreen
import me.novavpn.vpn.feature.auth.AuthScreen
import me.novavpn.vpn.feature.auth.ResetPasswordScreen
import me.novavpn.vpn.feature.home.HomeScreen
import me.novavpn.vpn.feature.proxies.ProxyScreen
import me.novavpn.vpn.feature.servers.ServersScreen
import me.novavpn.vpn.feature.settings.SettingsScreen
import me.novavpn.vpn.model.AccountRole
import me.novavpn.vpn.model.UserAccount
import me.novavpn.vpn.model.ConnectionProfile
import me.novavpn.vpn.model.TunnelProtocol
import me.novavpn.vpn.ui.theme.*
import me.novavpn.vpn.viewmodel.NovaVPNViewModel
import me.novavpn.vpn.viewmodel.ServersViewModel
import me.novavpn.vpn.viewmodel.SettingsViewModel
import me.novavpn.vpn.viewmodel.SessionStartup

private enum class AppTab(val label: String, @DrawableRes val icon: Int) {
    HOME("Inicio", R.drawable.nova_nav_home),
    SERVERS("VPN", R.drawable.nova_nav_vpn),
    PROXIES("Proxies", R.drawable.nova_nav_proxy),
    SETTINGS("Ajustes", R.drawable.nova_nav_settings),
    PROFILE("Cuenta", R.drawable.nova_nav_account),
    ADMIN("Admin", R.drawable.nova_nav_admin),
}

@Composable
fun NovaVPNApp(resetToken:String?=null,onResetConsumed:()->Unit={},viewModel:NovaVPNViewModel=viewModel(),serversViewModel:ServersViewModel=viewModel(),settingsViewModel:SettingsViewModel=viewModel()){
    val startup by viewModel.startup.collectAsState()
    val user by viewModel.user.collectAsState(); val error by viewModel.authError.collectAsState(); val message by viewModel.authMessage.collectAsState(); val loading by viewModel.authLoading.collectAsState()
    when{ startup == SessionStartup.RESTORING -> NovaSplashScreen(); resetToken!=null->ResetPasswordScreen(resetToken,error,message,loading,viewModel::resetPassword,onResetConsumed); user==null->AuthScreen(error,message,loading,viewModel::login,viewModel::register,viewModel::forgotPassword,viewModel::clearAuthMessage); else->MainShell(user!!,viewModel,serversViewModel,settingsViewModel) }
}

@Composable private fun MainShell(user:UserAccount,app:NovaVPNViewModel,servers:ServersViewModel,settings:SettingsViewModel){
    var tab by rememberSaveable{mutableStateOf(AppTab.HOME)};
    val connection by app.connection.collectAsState()
    val serverState by servers.state.collectAsState()
    val preferences by settings.preferences.collectAsState()
    val metrics by app.adminMetrics.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(preferences) { app.setPreferences(preferences) }
    var pendingConnect by remember { mutableStateOf(false) }
    val vpnPermission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (pendingConnect) { pendingConnect = false; app.toggleConnection() }
    }
    val toggleWithPermission = {
        if (connection.phase == me.novavpn.vpn.model.ConnectionPhase.CONNECTED) app.toggleConnection()
        else {
            val intent = VpnService.prepare(context)
            if (intent == null) app.toggleConnection() else { pendingConnect = true; vpnPermission.launch(intent) }
        }
    }
    val connectProfile: (ConnectionProfile) -> Unit = { profile ->
        val region = when (profile) {
            ConnectionProfile.FASTEST -> serverState.regions.firstOrNull()
            ConnectionProfile.GAMING -> serverState.regions.filter { it.loadPercent < 45 }.minByOrNull { serverState.stats[it.code]?.latencyMs ?: it.latencyMs }
            ConnectionProfile.STREAMING -> app.regions.firstOrNull { it.code == "use2" }
            ConnectionProfile.PRIVACY -> app.regions.firstOrNull { it.code == "se" }
        } ?: app.regions.first()
        app.selectRegion(region)
        app.selectProtocol(TunnelProtocol.WIREGUARD)
        val intent = VpnService.prepare(context)
        if (intent == null) app.toggleConnection() else { pendingConnect = true; vpnPermission.launch(intent) }
    }
    val profileTab=if(user.role==AccountRole.ADMIN) AppTab.ADMIN else AppTab.PROFILE; val tabs=listOf(AppTab.HOME,AppTab.SERVERS,AppTab.PROXIES,AppTab.SETTINGS,profileTab)
    Scaffold(containerColor=Night,bottomBar={NavigationBar(containerColor=DeepSurface){tabs.forEach{item->NavigationBarItem(selected=tab==item,onClick={tab=item},icon={Icon(painterResource(item.icon),null)},label={Text(item.label,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Cyan,selectedTextColor=Cyan,indicatorColor=Cyan.copy(.1f),unselectedIconColor=Muted,unselectedTextColor=Muted))}}}){padding->
        Box(Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(Color(0xFF091222),Night,Night)))){AnimatedContent(tab,label="feature"){destination->when(destination){AppTab.HOME->HomeScreen(connection,app.regions,toggleWithPermission,app::selectRegion,app::selectProtocol,connectProfile);AppTab.SERVERS->ServersScreen(serverState,connection.region,servers::setSort,servers::setContinent,servers::refreshLatencies,app::selectRegion);AppTab.PROXIES->ProxyScreen { region ->
    val intent = VpnService.prepare(context)
    if (intent == null) app.connectProxy(region) else { pendingConnect = true; app.selectRegion(region); app.selectProtocol(me.novavpn.vpn.model.TunnelProtocol.SOCKS5); vpnPermission.launch(intent) }
};AppTab.SETTINGS->SettingsScreen(preferences,{settings.setProtocol(it);app.selectProtocol(it)},settings::setAutoConnect,settings::setKillSwitch,settings::setLanAccess,settings::setSplitTunneling,settings::setTcpFallback,settings::setNetShield,settings::setVpnAccelerator);AppTab.PROFILE->AccountScreen(user,app::logout);AppTab.ADMIN->AdminScreen(metrics,app.regions)}}}
    }
}
