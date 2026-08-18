package me.novavpn.vpn.data

import me.novavpn.vpn.model.VpnRegion

object ServiceCatalog {
    val regions = listOf(
        VpnRegion("mx", "Querétaro", "México", "Latinoamérica", "🇲🇽", "mx.vpn.echosmart.me", "mx.socks5.echosmart.me", 28, 31, 2),
        VpnRegion("use2", "Virginia", "Estados Unidos", "América", "🇺🇸", "use2.vpn.echosmart.me", "use2.socks5.echosmart.me", 42, 39, 1),
        VpnRegion("usw3", "Arizona", "Estados Unidos", "América", "🇺🇸", "usw3.vpn.echosmart.me", "usw3.socks5.echosmart.me", 55, 34, 1),
        VpnRegion("br", "São Paulo", "Brasil", "Latinoamérica", "🇧🇷", "br.vpn.echosmart.me", "br.socks5.echosmart.me", 86, 46, 1),
        VpnRegion("cl", "Santiago", "Chile", "Latinoamérica", "🇨🇱", "cl.vpn.echosmart.me", "cl.socks5.echosmart.me", 94, 27, 1),
        VpnRegion("fr", "París", "Francia", "Europa", "🇫🇷", "fr.vpn.echosmart.me", "fr.socks5.echosmart.me", 112, 29, 1),
        VpnRegion("se", "Gävle", "Suecia", "Europa", "🇸🇪", "se.vpn.echosmart.me", "se.socks5.echosmart.me", 126, 25, 1),
        VpnRegion("in", "Pune", "India", "Asia", "🇮🇳", "in.vpn.echosmart.me", "in.socks5.echosmart.me", 184, 41, 1),
        VpnRegion("jp", "Tokio", "Japón", "Asia", "🇯🇵", "jp.vpn.echosmart.me", "jp.socks5.echosmart.me", 162, 36, 1),
        VpnRegion("hk", "Hong Kong", "Asia Oriental", "Asia", "🇭🇰", "hk.vpn.echosmart.me", "hk.socks5.echosmart.me", 171, 33, 1),
        VpnRegion("uk", "Londres", "Reino Unido", "Europa", "🇬🇧", "uk.vpn.echosmart.me", "uk.socks5.echosmart.me", 118, 30, 1),
        VpnRegion("au", "Sídney", "Australia", "Oceanía", "🇦🇺", "au.vpn.echosmart.me", "au.socks5.echosmart.me", 218, 26, 1),
        VpnRegion("kr", "Seúl", "Corea del Sur", "Asia", "🇰🇷", "kr.vpn.echosmart.me", "kr.socks5.echosmart.me", 178, 28, 1),
        VpnRegion("uae", "Dubái", "Emiratos Árabes", "Oriente Medio", "🇦🇪", "uae.vpn.echosmart.me", "uae.socks5.echosmart.me", 196, 24, 1),
        VpnRegion("za", "Johannesburgo", "Sudáfrica", "África", "🇿🇦", "za.vpn.echosmart.me", "za.socks5.echosmart.me", 205, 32, 1),
    )

    val fastest: VpnRegion get() = regions.minBy { it.latencyMs }
}

val ServiceCatalog.proxies: List<me.novavpn.vpn.model.ProxyEndpoint>
    get() = regions.flatMap { region ->
        listOf(
            me.novavpn.vpn.model.ProxyEndpoint("${region.code}-socks5", region, me.novavpn.vpn.model.ProxyType.SOCKS5, region.proxyHost),
            me.novavpn.vpn.model.ProxyEndpoint("${region.code}-https", region, me.novavpn.vpn.model.ProxyType.HTTPS, region.proxyHost),
            me.novavpn.vpn.model.ProxyEndpoint("${region.code}-socks4", region, me.novavpn.vpn.model.ProxyType.SOCKS4, region.proxyHost),
        )
    }
