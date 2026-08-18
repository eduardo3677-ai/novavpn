package me.novavpn.vpn.feature.proxies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.AppHeader
import me.novavpn.vpn.core.ui.NovaAssets
import me.novavpn.vpn.data.ServiceCatalog
import me.novavpn.vpn.data.proxies
import me.novavpn.vpn.model.*
import me.novavpn.vpn.ui.theme.*

@Composable
fun ProxyScreen(onConnect: (VpnRegion) -> Unit) {
    var type by remember { mutableStateOf(ProxyType.SOCKS5) }
    val clipboard = LocalClipboardManager.current
    val endpoints = ServiceCatalog.proxies.filter { it.type == type }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.padding(20.dp)) { AppHeader("Proxies privados", "SOCKS4/5 y HTTPS autenticados") }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProxyType.entries) { item ->
                FilterChip(selected = type == item, onClick = { type = item }, label = { Text(item.title) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(endpoints, key = { it.id }) { endpoint ->
                ProxyRow(endpoint, onConnect = { onConnect(endpoint.region) }, onCopy = {
                    clipboard.setText(AnnotatedString("${endpoint.host}:${endpoint.type.port}"))
                })
            }
        }
    }
}

@Composable
private fun ProxyRow(endpoint: ProxyEndpoint, onConnect: () -> Unit, onCopy: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(Glass)
            .border(1.dp, Color.White.copy(.06f), RoundedCornerShape(19.dp)).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(endpoint.typeColor().copy(.12f)),
            contentAlignment = Alignment.Center,
        ) { Icon(painterResource(NovaAssets.proxy(endpoint.type)), null, tint = endpoint.typeColor()) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("${endpoint.region.flag} ${endpoint.region.city}", fontWeight = FontWeight.SemiBold)
            Text(
                "${endpoint.host}:${endpoint.type.port}", color = Muted, fontSize = 11.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Mint))
                Spacer(Modifier.width(6.dp))
                Text(if (endpoint.authenticated) "Autenticado" else "Disponible", color = Mint, fontSize = 10.sp)
            }
        }
        if (endpoint.type == ProxyType.SOCKS5) {
            FilledTonalButton(onClick = onConnect, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text("Conectar", fontSize = 12.sp)
            }
        }
        IconButton(onClick = onCopy) { Icon(Icons.Rounded.ContentCopy, null, tint = Muted) }
    }
}

private fun ProxyEndpoint.typeColor() = when (type) {
    ProxyType.SOCKS5 -> Color(0xFFFFB86B)
    ProxyType.HTTPS -> Color(0xFFF472B6)
    ProxyType.SOCKS4 -> Color(0xFFF59E0B)
}
