package com.scpsync.plugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.scpsync.plugin.config.ScpSyncConfig
import com.scpsync.plugin.services.ScpSyncService

class ScpSyncPluginTest : BasePlatformTestCase() {

    fun testConfigurationPersistence() {
        val config = ScpSyncConfig.getInstance()
        
        // Test default values
        assertEquals("", config.serverIp)
        assertEquals("", config.username)
        assertEquals(22, config.port)
        assertFalse(config.isEnabled)
        
        // Test setting values
        config.serverIp = "192.168.1.100"
        config.username = "testuser"
        config.port = 2222
        config.isEnabled = true
        
        assertEquals("192.168.1.100", config.serverIp)
        assertEquals("testuser", config.username)
        assertEquals(2222, config.port)
        assertTrue(config.isEnabled)
    }
    
    fun testScpSyncService() {
        val service = ScpSyncService()
        assertNotNull(service)
    }
}