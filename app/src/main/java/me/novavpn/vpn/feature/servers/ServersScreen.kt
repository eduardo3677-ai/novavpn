package me.novavpn.vpn.feature.servers

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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.AppHeader
import me.novavpn.vpn.model.*
import me.novavpn.vpn.ui.theme.*
import me.novavpn.vpn.viewmodel.ServersUiState

@Composable
fun ServersScreen(
    state: ServersUiState, 
    selected: VpnRegion, 
    onSort: (ServerSort) -> Unit, 
    onContinent: (String) -> Unit, 
    onRefresh: () -> Unit, 
    onSelect: (VpnRegion) -> Unit
) {
    var sortMenu by remember { mutableStateOf(false) }
    val filters = listOf("Todos", "Latinoamérica", "América", "Europa", "Asia", "Oceanía", "Oriente Medio", "África")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(Modifier.padding(20.dp)) { 
            AppHeader("Servidores VPN", "Latencia y carga en tiempo real") { 
                IconButton(onClick = onRefresh) { 
                    if (state.refreshing) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Cyan)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Cyan)
                    }
                } 
            }
            
            Spacer(Modifier.height(16.dp))
            
            Box { 
                AssistChip(
                    onClick = { sortMenu = true },
                    label = { Text(state.sort.title) }
                ) 
                DropdownMenu(
                    expanded = sortMenu,
                    onDismissRequest = { sortMenu = false },
                    modifier = Modifier.background(DeepSurface)
                ) { 
                    ServerSort.entries.forEach { 
                        DropdownMenuItem(
                            text = { Text(it.title) },
                            onClick = { 
                                onSort(it)
                                sortMenu = false 
                            }
                        ) 
                    } 
                } 
            } 
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) { 
            items(filters) { 
                FilterChip(
                    selected = state.continent == it,
                    onClick = { onContinent(it) },
                    label = { Text(it) }
                ) 
            } 
        }
        
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) { 
            items(state.regions, key = { it.code }) { region -> 
                ServerRow(
                    region = region,
                    stats = state.stats[region.code],
                    selected = region == selected
                ) {
                    onSelect(region)
                } 
            } 
        }
    }
}

@Composable 
private fun ServerRow(
    region: VpnRegion, 
    stats: ServerStats?, 
    selected: Boolean,
    onClick: () -> Unit
) { 
    val latency = stats?.latencyMs ?: region.latencyMs
    val isOffline = stats?.available == false
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Cyan.copy(alpha = 0.1f) else Glass)
            .border(1.dp, if (selected) Cyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { 
        Text(region.flag, fontSize = 28.sp)
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Text(region.city, fontWeight = FontWeight.SemiBold)
            Text(
                "${region.country} · ${region.nodes} nodo${if(region.nodes > 1) "s" else ""}",
                color = Muted,
                fontSize = 12.sp
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (isOffline) "Offline" else "$latency ms",
                color = if (isOffline) Danger else if (latency < 80) Mint else Cyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "jitter ${stats?.jitterMs ?: 0} · ${region.loadPercent}%",
                color = Muted,
                fontSize = 11.sp
            )
        }
        
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isOffline) Danger else Mint)
        ) 
    } 
}
