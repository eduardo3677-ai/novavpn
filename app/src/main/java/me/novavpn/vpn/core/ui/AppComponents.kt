package me.novavpn.vpn.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.ui.theme.*

@Composable
fun AppHeader(title: String, subtitle: String, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(title, style = MaterialTheme.typography.headlineMedium); Text(subtitle, color = Muted, fontSize = 13.sp) }
        action?.invoke() ?: Box(
            Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan.copy(.25f), Violet.copy(.25f))))
                .border(1.dp, Cyan.copy(.35f), CircleShape), contentAlignment = Alignment.Center
        ) { Image(painterResource(NovaAssets.brandMark), "NovaVPN", modifier = Modifier.size(38.dp)) }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, color = Muted, fontSize = 12.sp) }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(22.dp)).background(Glass.copy(.78f)).border(1.dp, Color.White.copy(.06f), RoundedCornerShape(22.dp)).padding(18.dp), content = content)
}

@Composable
fun EchoTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, password: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        label = { Text(label) }, leadingIcon = { Icon(icon, null, tint = Cyan) }, singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan, unfocusedBorderColor = Color.White.copy(.12f), focusedContainerColor = Glass.copy(.55f), unfocusedContainerColor = Glass.copy(.40f)),
    )
}

@Composable
fun LabelValue(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 13.sp); Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

fun formatDuration(seconds: Long): String = "%02d:%02d".format(seconds / 60, seconds % 60)
