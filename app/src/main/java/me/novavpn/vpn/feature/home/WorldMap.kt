package me.novavpn.vpn.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.model.VpnRegion
import me.novavpn.vpn.ui.theme.Cyan
import me.novavpn.vpn.ui.theme.Glass
import me.novavpn.vpn.ui.theme.Mint
import me.novavpn.vpn.ui.theme.Muted

private val coordinates = mapOf(
    "mx" to (.19f to .48f), "use2" to (.25f to .39f), "usw3" to (.12f to .40f),
    "br" to (.34f to .70f), "cl" to (.27f to .80f), "fr" to (.49f to .35f),
    "se" to (.52f to .22f), "uk" to (.46f to .30f), "in" to (.68f to .52f),
    "uae" to (.61f to .48f), "za" to (.55f to .78f), "jp" to (.89f to .42f),
    "kr" to (.85f to .43f), "hk" to (.81f to .53f), "au" to (.88f to .80f),
)

@Composable
fun WorldMapCard(regions: List<VpnRegion>, selected: VpnRegion, onSelect: (VpnRegion) -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Glass, RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(.06f), RoundedCornerShape(28.dp)).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("Mapa global", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Toca una región para seleccionarla", color = Muted, fontSize = 12.sp) }
            Text("${regions.size} online", color = Mint, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        Canvas(
            Modifier.fillMaxWidth().height(126.dp).pointerInput(regions, selected) {
                detectTapGestures { tap ->
                    regions.minByOrNull { region ->
                        val point = coordinates[region.code] ?: (.5f to .5f)
                        val dx = tap.x - size.width * point.first
                        val dy = tap.y - size.height * point.second
                        dx * dx + dy * dy
                    }?.let(onSelect)
                }
            }
        ) {
            val grid = Color.White.copy(alpha = .055f)
            repeat(7) { i -> drawLine(grid, Offset(0f, size.height * i / 6f), Offset(size.width, size.height * i / 6f), 1f) }
            repeat(9) { i -> drawLine(grid, Offset(size.width * i / 8f, 0f), Offset(size.width * i / 8f, size.height), 1f) }
            val land = Path().apply {
                moveTo(size.width*.04f,size.height*.26f); lineTo(size.width*.20f,size.height*.13f); lineTo(size.width*.34f,size.height*.25f); lineTo(size.width*.29f,size.height*.48f); lineTo(size.width*.38f,size.height*.66f); lineTo(size.width*.30f,size.height*.92f); lineTo(size.width*.19f,size.height*.72f); lineTo(size.width*.13f,size.height*.50f); close()
                moveTo(size.width*.42f,size.height*.18f); lineTo(size.width*.62f,size.height*.12f); lineTo(size.width*.76f,size.height*.27f); lineTo(size.width*.94f,size.height*.30f); lineTo(size.width*.88f,size.height*.55f); lineTo(size.width*.72f,size.height*.54f); lineTo(size.width*.62f,size.height*.78f); lineTo(size.width*.48f,size.height*.67f); close()
                moveTo(size.width*.80f,size.height*.72f); lineTo(size.width*.96f,size.height*.75f); lineTo(size.width*.93f,size.height*.94f); lineTo(size.width*.79f,size.height*.90f); close()
            }
            drawPath(land, Color(0xFF173044).copy(alpha=.72f)); drawPath(land, Cyan.copy(alpha=.12f), style=Stroke(1.dp.toPx()))
            regions.forEach { region ->
                val point = coordinates[region.code] ?: return@forEach
                val center = Offset(size.width*point.first,size.height*point.second)
                val active = region.code == selected.code
                drawCircle(if(active) Cyan.copy(.18f) else Mint.copy(.10f), if(active) 18.dp.toPx() else 11.dp.toPx(), center)
                drawCircle(if(active) Cyan else Mint, if(active) 6.dp.toPx() else 4.dp.toPx(), center)
            }
        }
    }
}
