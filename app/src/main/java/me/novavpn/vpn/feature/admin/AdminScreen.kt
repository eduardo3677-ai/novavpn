package me.novavpn.vpn.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.*
import me.novavpn.vpn.model.*
import me.novavpn.vpn.ui.theme.*

@Composable fun AdminScreen(metrics:AdminMetrics,regions:List<VpnRegion>){LazyColumn(Modifier.fillMaxSize().statusBarsPadding(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{AppHeader("Centro de control","Administrador NovaVPN")};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("Clientes",metrics.registeredClients.toString(),Icons.Rounded.Groups,Cyan,Modifier.weight(1f));Metric("Sesiones",metrics.activeSessions.toString(),Icons.Rounded.Link,Mint,Modifier.weight(1f))}};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("Nodos",metrics.onlineNodes.toString(),Icons.Rounded.Dns,Violet,Modifier.weight(1f));Metric("Gbps",metrics.throughputGbps.toString(),Icons.Rounded.Speed,Color(0xFFFFB86B),Modifier.weight(1f))}};item{SectionHeader("Servicios registrados","Estado de infraestructura Azure")};items(regions){r->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Glass).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(r.flag,fontSize=24.sp);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(r.city,fontWeight=FontWeight.SemiBold);Text("${r.nodes} nodo(s) · ${r.loadPercent}%",color=Muted,fontSize=11.sp)};Box(Modifier.size(8.dp).clip(CircleShape).background(Mint));Spacer(Modifier.width(7.dp));Text("Online",color=Mint,fontSize=11.sp)}}}}
@Composable private fun Metric(title:String,value:String,icon:ImageVector,color:Color,modifier:Modifier){Column(modifier.clip(RoundedCornerShape(20.dp)).background(Glass).padding(16.dp)){Icon(icon,null,tint=color);Spacer(Modifier.height(12.dp));Text(value,style=MaterialTheme.typography.titleLarge);Text(title,color=Muted,fontSize=12.sp)}}
