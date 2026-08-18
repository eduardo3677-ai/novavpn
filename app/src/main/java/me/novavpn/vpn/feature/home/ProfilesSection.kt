package me.novavpn.vpn.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import me.novavpn.vpn.core.ui.NovaAssets
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.novavpn.vpn.model.ConnectionProfile
import me.novavpn.vpn.ui.theme.*

@Composable
fun ProfilesSection(onProfile: (ConnectionProfile) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(ConnectionProfile.entries) { profile ->
            val color = profileColor(profile)
            Column(
                Modifier.width(180.dp).clip(RoundedCornerShape(20.dp)).background(Glass)
                    .border(1.dp, Color.White.copy(.06f), RoundedCornerShape(20.dp))
                    .clickable { onProfile(profile) }.padding(16.dp)
            ) {
                Icon(painterResource(NovaAssets.profile(profile)), null, tint = color)
                Spacer(Modifier.height(14.dp))
                Text(profile.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(profile.description, color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

private fun profileColor(profile: ConnectionProfile): Color = when(profile) {
    ConnectionProfile.FASTEST -> Cyan
    ConnectionProfile.GAMING -> Mint
    ConnectionProfile.STREAMING -> Color(0xFFFFB86B)
    ConnectionProfile.PRIVACY -> Violet
}
