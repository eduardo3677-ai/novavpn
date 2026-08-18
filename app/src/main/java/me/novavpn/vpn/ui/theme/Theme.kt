package me.novavpn.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Night = Color(0xFF0E1015)
val DeepSurface = Color(0xFF171A21)
val Glass = Color(0xFF232732)
val Cyan = Color(0xFF00B167) // Proton Emerald
val Violet = Color(0xFF7552CC) // Muted purple accent
val Mint = Color(0xFF5AE8A2) // Light emerald
val Muted = Color(0xFF8FA3BC)
val Danger = Color(0xFFFF4B4B)

private val NovaColors = darkColorScheme(
    primary = Cyan,
    secondary = Violet,
    tertiary = Mint,
    background = Night,
    surface = DeepSurface,
    surfaceVariant = Glass,
    onPrimary = Night,
    onBackground = Color(0xFFF4F8FF),
    onSurface = Color(0xFFF4F8FF),
    onSurfaceVariant = Muted,
    error = Danger,
)

private val EchoTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.2.sp),
)

@Composable
fun NovaVPNTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NovaColors, typography = EchoTypography, content = content)
}
