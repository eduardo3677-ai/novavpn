package me.novavpn.vpn.feature.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import me.novavpn.vpn.core.ui.NovaAssets
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.core.ui.EchoTextField
import me.novavpn.vpn.ui.theme.*

@Composable
fun AuthScreen(error: String?, message: String?, loading: Boolean, onLogin: (String, String) -> Unit, onRegister: (String, String, String, String, String) -> Unit, onForgot: (String) -> Unit, onClear: () -> Unit) {
    var register by rememberSaveable { mutableStateOf(false) }; var first by rememberSaveable { mutableStateOf("") }; var last by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }; var email by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var forgot by rememberSaveable { mutableStateOf(false) }
    if (forgot) AlertDialog(onDismissRequest = { forgot = false }, title = { Text("Recuperar contraseña") }, text = { EchoTextField(email, { email = it }, "Correo", Icons.Rounded.AlternateEmail, KeyboardType.Email) }, confirmButton = { TextButton(onClick = { onForgot(email); forgot = false }) { Text("Enviar enlace") } }, dismissButton = { TextButton(onClick = { forgot = false }) { Text("Cancelar") } }, containerColor = DeepSurface)
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF142443), Night), Offset(300f, 180f), 900f)).statusBarsPadding().padding(24.dp)) {
        Canvas(Modifier.fillMaxSize()) { drawCircle(Cyan.copy(.08f), size.minDimension * .55f, Offset(size.width * .92f, size.height * .12f)); drawCircle(Violet.copy(.10f), size.minDimension * .42f, Offset(size.width * .08f, size.height * .82f)) }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) { item {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Cyan.copy(.18f), Violet.copy(.18f)))), contentAlignment = Alignment.Center) { Image(painterResource(NovaAssets.brandMark), "NovaVPN", modifier = Modifier.size(50.dp)) }; Spacer(Modifier.width(14.dp)); Column { Text("NovaVPN", style = MaterialTheme.typography.headlineMedium); Text("Private global network", color = Muted) } }
            Spacer(Modifier.height(32.dp)); Text(if (register) "Crea tu identidad privada" else "Bienvenido de nuevo", style = MaterialTheme.typography.displaySmall); Text("Autenticación real y control global.", color = Muted); Spacer(Modifier.height(22.dp))
            if (register) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { EchoTextField(first, { first = it }, "Nombre", Icons.Rounded.Badge) }; Box(Modifier.weight(1f)) { EchoTextField(last, { last = it }, "Apellidos", Icons.Rounded.Badge) } }; EchoTextField(username, { username = it }, "Usuario", Icons.Rounded.AccountCircle) }
            EchoTextField(email, { email = it }, "Correo", Icons.Rounded.AlternateEmail, KeyboardType.Email); EchoTextField(password, { password = it }, "Contraseña", Icons.Rounded.Key, password = true)
            error?.let { Text(it, color = Danger, fontSize = 13.sp) }; message?.let { Text(it, color = Mint, fontSize = 13.sp) }; Spacer(Modifier.height(12.dp))
            Button(onClick = { if (register) onRegister(first, last, username, email, password) else onLogin(email, password) }, enabled = !loading, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)) { if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Night) else Text(if (register) "Crear cuenta" else "Entrar", fontWeight = FontWeight.Bold) }
            if (!register) TextButton(onClick = { forgot = true }, modifier = Modifier.fillMaxWidth()) { Text("Olvidé mi contraseña", color = Muted) }
            TextButton(onClick = { register = !register; onClear() }, modifier = Modifier.fillMaxWidth()) { Text(if (register) "Ya tengo una cuenta" else "Crear una cuenta nueva", color = Cyan) }
        } }
    }
}

@Composable
fun ResetPasswordScreen(token: String, error: String?, message: String?, loading: Boolean, onReset: (String, String, () -> Unit) -> Unit, onDone: () -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }; var confirmation by rememberSaveable { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF111E35), Night))).statusBarsPadding().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Glass).padding(22.dp)) { Icon(Icons.Rounded.Password, null, tint = Cyan, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(16.dp)); Text("Nueva contraseña", style = MaterialTheme.typography.headlineMedium); Text("El enlace se validará una sola vez.", color = Muted); Spacer(Modifier.height(20.dp)); EchoTextField(password, { password = it }, "Contraseña nueva", Icons.Rounded.Key, password = true); EchoTextField(confirmation, { confirmation = it }, "Confirmar contraseña", Icons.Rounded.Key, password = true); error?.let { Text(it, color = Danger) }; message?.let { Text(it, color = Mint) }; Spacer(Modifier.height(12.dp)); Button(onClick = { onReset(token, password, onDone) }, enabled = !loading && password.length >= 10 && password == confirmation, modifier = Modifier.fillMaxWidth()) { Text("Actualizar contraseña") } }
    }
}
