package me.novavpn.vpn.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.AppHeader
import me.novavpn.vpn.model.UserAccount
import me.novavpn.vpn.ui.theme.*

@Composable fun AccountScreen(user:UserAccount,onLogout:()->Unit){LazyColumn(Modifier.fillMaxSize().statusBarsPadding(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{AppHeader("Tu cuenta",user.plan)};item{Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(androidx.compose.ui.graphics.Color(0xFF172D44),androidx.compose.ui.graphics.Color(0xFF251B45)))).padding(20.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(58.dp).clip(CircleShape).background(Cyan.copy(.15f)),contentAlignment=Alignment.Center){Text(user.firstName.take(1).uppercase(),color=Cyan,fontSize=24.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(user.name,style=MaterialTheme.typography.titleLarge);Text("@${user.username}",color=Muted);Text(user.email,color=Muted,fontSize=12.sp)};Icon(Icons.Rounded.Verified,null,tint=Mint)}};item{Option(Icons.Rounded.Diamond,"Plan ${user.plan}","Velocidad y regiones globales")};item{Option(Icons.Rounded.Devices,"Dispositivos","Gestionar sesiones autorizadas")};item{Option(Icons.Rounded.Security,"Seguridad","Contraseña y recuperación")};item{OutlinedButton(onClick=onLogout,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.outlinedButtonColors(contentColor=Danger)){Icon(Icons.Rounded.Logout,null);Spacer(Modifier.width(8.dp));Text("Cerrar sesión")}}}}
@Composable private fun Option(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,subtitle:String){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Glass).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Cyan);Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.SemiBold);Text(subtitle,color=Muted,fontSize=12.sp)};Icon(Icons.Rounded.ChevronRight,null,tint=Muted)}}
