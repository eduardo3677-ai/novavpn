package me.novavpn.vpn

import me.novavpn.vpn.data.ServiceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceCatalogTest {
    @Test fun catalog_has_deployed_regions() {
        assertEquals(15, ServiceCatalog.regions.size)
        assertTrue(ServiceCatalog.regions.all { it.vpnHost.endsWith(".echosmart.me") })
        assertEquals("mx", ServiceCatalog.fastest.code)
    }
}
