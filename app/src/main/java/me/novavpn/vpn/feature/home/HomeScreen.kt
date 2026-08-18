package me.novavpn.vpn.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.AppHeader
import me.novavpn.vpn.core.ui.GlassCard
import me.novavpn.vpn.core.ui.LabelValue
import me.novavpn.vpn.core.ui.NovaAssets
import me.novavpn.vpn.core.ui.SectionHeader
import me.novavpn.vpn.core.ui.formatDuration
import me.novavpn.vpn.model.ConnectionPhase
import me.novavpn.vpn.model.ConnectionProfile
import me.novavpn.vpn.model.ConnectionUiState
import me.novavpn.vpn.model.TunnelProtocol
import me.novavpn.vpn.model.VpnRegion
import me.novavpn.vpn.ui.theme.*

@Composable
fun HomeScreen(
    state: ConnectionUiState,
    regions: List<VpnRegion>,
    onToggle: () -> Unit,
    onRegion: (VpnRegion) -> Unit,
    onProtocol: (TunnelProtocol) -> Unit,
    onProfile: (ConnectionProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { 
            AppHeader("NovaVPN", "Privacidad sin fronteras") 
        }
        item { 
            ConnectionPanel(state, onToggle) 
        }
        item { 
            WorldMapCard(regions, state.region, onRegion) 
        }
        item { 
            QuickRegion(state.region, regions, onRegion) 
        }
        item { 
            SectionHeader("Perfiles Rápidos", "Conecta según tu actividad")
            Spacer(Modifier.height(12.dp))
            ProfilesSection(onProfile) 
        }
        item { 
            SectionHeader("Protocolo", "Motor de conexión preferido")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { 
                items(TunnelProtocol.entries) { 
                    ProtocolTile(it, it == state.protocol) { onProtocol(it) } 
                } 
            } 
        }
        item { 
            GlassCard { 
                SectionHeader("Detalles de red", "Infraestructura Segura")
                Spacer(Modifier.height(12.dp))
                LabelValue("Host VPN", state.region.vpnHost)
                LabelValue("IP pública", state.publicIp)
                LabelValue("Carga regional", "${state.region.loadPercent}%", Mint) 
            } 
        }
    }
}

@Composable 
private fun ConnectionPanel(state: ConnectionUiState, onToggle: () -> Unit) {
    val isConnected = state.phase == ConnectionPhase.CONNECTED
    val accent by animateColorAsState(if (isConnected) Mint else Cyan, label = "connection")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(DeepSurface, Night)))
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isConnected) "CONEXIÓN SEGURA" else if (state.phase == ConnectionPhase.CONNECTING) "CONECTANDO..." else "LISTO PARA PROTEGER", 
            color = accent, 
            fontSize = 13.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 1.2.sp
        )
        
        Spacer(Modifier.height(24.dp))
        ConnectionOrb(state, accent, onToggle)
        Spacer(Modifier.height(24.dp))
        
        Text("${state.region.flag} ${state.region.city}", style = MaterialTheme.typography.titleLarge)
        Text("${state.protocol.title} · ${state.region.latencyMs} ms", color = Muted)
        
        if (isConnected) { 
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceEvenly
            ) { 
                MiniMetric("↓", "${state.downloadMbps.toInt()} Mbps")
                MiniMetric("↑", "${state.uploadMbps.toInt()} Mbps")
                MiniMetric("◷", formatDuration(state.connectedSeconds)) 
            } 
        }
    }
}

@Composable 
private fun ConnectionOrb(state: ConnectionUiState, accent: Color, onToggle: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "pulse")
        .animateFloat(0.9f, 1.1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulseValue")
    
    val isConnected = state.phase == ConnectionPhase.CONNECTED
    
    Box(
        modifier = Modifier
            .size(140.dp)
            .clickable(
                enabled = state.phase == ConnectionPhase.DISCONNECTED || isConnected, 
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) { 
            val r = size.minDimension / 2
            if (isConnected) {
                drawCircle(accent.copy(alpha = 0.1f), r * pulse)
            }
            drawArc(
                Brush.sweepGradient(listOf(accent.copy(alpha=0.1f), accent, Night, accent.copy(alpha=0.1f))), 
                -90f, 
                if (isConnected) 360f else 280f, 
                false, 
                Offset(8f, 8f), 
                Size(size.width - 16, size.height - 16), 
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(Glass, r * 0.75f) 
        }
        Icon(
            if (isConnected) Icons.Rounded.PowerSettingsNew else Icons.Rounded.Shield, 
            contentDescription = null, 
            tint = accent, 
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable 
private fun MiniMetric(icon: String, value: String) { 
    Column(horizontalAlignment = Alignment.CenterHorizontally) { 
        Text(icon, color = Cyan)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) 
    } 
}

@Composable 
private fun QuickRegion(selected: VpnRegion, regions: List<VpnRegion>, onSelect: (VpnRegion) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column { 
        SectionHeader("Conexión rápida", "Selección inteligente por latencia")
        Spacer(Modifier.height(12.dp))
        
        Box { 
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Glass)
                    .clickable { expanded = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { 
                Text(selected.flag, fontSize = 28.sp)
                Spacer(Modifier.width(16.dp))
                
                Column(Modifier.weight(1f)) { 
                    Text(selected.city, fontWeight = FontWeight.SemiBold)
                    Text("${selected.country} · ${selected.nodes} nodos", color = Muted, fontSize = 13.sp)
                }
                
                Text("${selected.latencyMs} ms", color = Mint, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.ExpandMore, null, tint = Muted) 
            }
            
            DropdownMenu(
                expanded = expanded, 
                onDismissRequest = { expanded = false }, 
                modifier = Modifier.background(DeepSurface)
            ) { 
                regions.forEach { region ->
                    DropdownMenuItem(
                        text = { Text("${region.flag} ${region.city} · ${region.latencyMs} ms") },
                        onClick = { onSelect(region); expanded = false }
                    ) 
                } 
            } 
        } 
    }
}

@Composable 
private fun ProtocolTile(protocol: TunnelProtocol, selected: Boolean, onClick: () -> Unit) { 
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) protocol.accent.copy(alpha = 0.15f) else Glass)
            .border(1.dp, if (selected) protocol.accent else Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) { 
        Icon(painterResource(NovaAssets.protocol(protocol)), null, tint = protocol.accent)
        Spacer(Modifier.height(12.dp))
        Text(protocol.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(protocol.subtitle, color = Muted, fontSize = 12.sp, lineHeight = 16.sp) 
    } 
}
