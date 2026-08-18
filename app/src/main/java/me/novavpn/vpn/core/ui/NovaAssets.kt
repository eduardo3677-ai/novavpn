package me.novavpn.vpn.core.ui

import androidx.annotation.DrawableRes
import me.novavpn.vpn.R
import me.novavpn.vpn.model.ConnectionProfile
import me.novavpn.vpn.model.ProxyType
import me.novavpn.vpn.model.TunnelProtocol

object NovaAssets {
    @DrawableRes val brandMark = R.drawable.nova_brand_mark
    @DrawableRes val notification = R.drawable.nova_notification

    @DrawableRes fun protocol(protocol: TunnelProtocol) = when (protocol) {
        TunnelProtocol.WIREGUARD -> R.drawable.nova_protocol_wireguard
        TunnelProtocol.OPENVPN_UDP, TunnelProtocol.OPENVPN_TCP -> R.drawable.nova_protocol_openvpn
        TunnelProtocol.IKEV2 -> R.drawable.nova_protocol_ikev2
        TunnelProtocol.SOCKS5, TunnelProtocol.SOCKS4 -> R.drawable.nova_protocol_socks
        TunnelProtocol.HTTPS -> R.drawable.nova_protocol_https
    }

    @DrawableRes fun profile(profile: ConnectionProfile) = when (profile) {
        ConnectionProfile.FASTEST -> R.drawable.nova_profile_fastest
        ConnectionProfile.GAMING -> R.drawable.nova_profile_gaming
        ConnectionProfile.STREAMING -> R.drawable.nova_profile_streaming
        ConnectionProfile.PRIVACY -> R.drawable.nova_profile_privacy
    }

    @DrawableRes fun proxy(type: ProxyType) = when (type) {
        ProxyType.SOCKS5, ProxyType.SOCKS4 -> R.drawable.nova_protocol_socks
        ProxyType.HTTPS -> R.drawable.nova_protocol_https
    }
}
