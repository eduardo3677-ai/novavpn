package me.novavpn.vpn.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.*
import me.novavpn.vpn.model.*
import me.novavpn.vpn.ui.theme.*

@Composable
fun SettingsScreen(
    preferences: ConnectionPreferences, 
    onProtocol: (TunnelProtocol) -> Unit,
    onAuto: (Boolean) -> Unit,
    onKill: (Boolean) -> Unit,
    onLan: (Boolean) -> Unit,
    onSplit: (Boolean) -> Unit,
    onTcp: (Boolean) -> Unit,
    onNetShield: (NetShieldMode) -> Unit,
    onAccelerator: (Boolean) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var shieldMenu by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { 
            AppHeader("Configuración", "Privacidad, protocolo y automatización") 
        }
        
        item { 
            GlassCard { 
                SectionHeader("Protocolo predeterminado", "Usado al conectar automáticamente")
                Spacer(Modifier.height(10.dp))
                
                Box { 
                    OutlinedButton(
                        onClick = { menu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        Icon(Icons.Rounded.Router, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(preferences.defaultProtocol.title)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.ExpandMore, contentDescription = null) 
                    }
                    
                    DropdownMenu(
                        expanded = menu,
                        onDismissRequest = { menu = false },
                        modifier = Modifier.background(DeepSurface)
                    ) { 
                        TunnelProtocol.entries.forEach { protocol ->
                            DropdownMenuItem(
                                text = { Text(protocol.title) },
                                onClick = { 
                                    onProtocol(protocol)
                                    menu = false 
                                }
                            ) 
                        } 
                    } 
                } 
            } 
        }
        
        item { 
            GlassCard { 
                SectionHeader("NetShield", "DNS seguro para bloquear amenazas")
                Spacer(Modifier.height(10.dp))
                
                Box { 
                    OutlinedButton(
                        onClick = { shieldMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        Icon(Icons.Rounded.Security, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(preferences.netShield.title)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.ExpandMore, contentDescription = null) 
                    }
                    
                    DropdownMenu(
                        expanded = shieldMenu,
                        onDismissRequest = { shieldMenu = false },
                        modifier = Modifier.background(DeepSurface)
                    ) { 
                        NetShieldMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.title) },
                                onClick = { 
                                    onNetShield(mode)
                                    shieldMenu = false 
                                }
                            ) 
                        } 
                    } 
                } 
            } 
        }
        
        item { 
            SettingToggle(
                icon = Icons.Rounded.Speed,
                title = "Acelerador VPN",
                subtitle = "Optimizar rutas y mantener el servidor más rápido",
                checked = preferences.vpnAccelerator,
                onChange = onAccelerator
            ) 
        }
        
        item { 
            SettingToggle(
                icon = Icons.Rounded.AutoAwesome,
                title = "Conexión automática",
                subtitle = "Conectar al iniciar o cambiar de red",
                checked = preferences.autoConnect,
                onChange = onAuto
            ) 
        }
        
        item { 
            SettingToggle(
                icon = Icons.Rounded.GppGood,
                title = "Kill switch",
                subtitle = "Bloquear tráfico si cae el túnel",
                checked = preferences.killSwitch,
                onChange = onKill
            ) 
        }
        
        item { 
            SettingToggle(
                icon = Icons.Rounded.Lan,
                title = "Acceso LAN",
                subtitle = "Permitir dispositivos de la red local",
                checked = preferences.lanAccess,
                onChange = onLan
            ) 
        }
        
        item { 
            SettingToggle(
                icon = Icons.Rounded.CallSplit,
                title = "Split tunneling",
                subtitle = "Elegir aplicaciones fuera de la VPN",
                checked = preferences.splitTunneling,
                onChange = onSplit
            ) 
        }
        
        item { 
            SettingToggle(
                icon = Icons.Rounded.SwapVert,
                title = "Fallback TCP",
                subtitle = "Usar TCP 443 si UDP está bloqueado",
                checked = preferences.useTcpFallback,
                onChange = onTcp
            ) 
        }
        
        item { 
            GlassCard { 
                SectionHeader("DNS y seguridad", "Resolución protegida en el túnel")
                Spacer(Modifier.height(8.dp))
                LabelValue("DNS", "1.1.1.1 / privado")
                LabelValue("IPv6", "Bloqueo de fugas")
                LabelValue("MTU", "Automático") 
            } 
        }
    }
}

@Composable 
private fun SettingToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) { 
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Glass)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { 
        Icon(icon, contentDescription = null, tint = Cyan)
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) { 
            Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Text(subtitle, color = Muted, fontSize = 12.sp) 
        }
        
        Switch(checked = checked, onCheckedChange = onChange) 
    } 
}
