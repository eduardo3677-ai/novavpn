package me.novavpn.vpn.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import me.novavpn.vpn.core.ui.NovaAssets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.ui.theme.*

@Composable
fun NovaSplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(.94f, 1.04f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "logo")
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(DeepSurface, Night))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(92.dp).scale(scale).clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) {
                Image(painterResource(NovaAssets.brandMark), "NovaVPN", modifier = Modifier.size(72.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("NovaVPN", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Private global network", color = Muted, fontSize = 14.sp)
        }
    }
}
