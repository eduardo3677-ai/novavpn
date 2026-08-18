package me.novavpn.vpn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.novavpn.vpn.ui.NovaVPNApp
import me.novavpn.vpn.ui.theme.NovaVPNTheme
import me.novavpn.vpn.viewmodel.NovaVPNViewModel
import me.novavpn.vpn.viewmodel.SessionStartup

class MainActivity : ComponentActivity() {
    private var resetToken by mutableStateOf<String?>(null)
    private val appViewModel: NovaVPNViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { appViewModel.startup.value == SessionStartup.RESTORING }
        resetToken = intent?.data?.getQueryParameter("token")
        enableEdgeToEdge()
        setContent {
            NovaVPNTheme {
                NovaVPNApp(
                    resetToken = resetToken,
                    onResetConsumed = { resetToken = null },
                    viewModel = appViewModel,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resetToken = intent.data?.getQueryParameter("token")
    }
}
