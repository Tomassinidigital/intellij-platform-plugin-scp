package com.scpsync.plugin.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.scpsync.plugin.config.ScpSyncConfig
import com.scpsync.plugin.services.ScpSyncService
import javax.swing.JComponent

class ScpSyncConfigurable : Configurable {
    
    private var configPanel: ScpSyncConfigPanel? = null
    private val config = ScpSyncConfig.getInstance()
    
    override fun getDisplayName(): String = "SCP File Synchronizer"
    
    override fun createComponent(): JComponent {
        configPanel = ScpSyncConfigPanel()
        reset()
        return configPanel!!
    }
    
    override fun isModified(): Boolean {
        val panel = configPanel ?: return false
        return panel.serverIp != config.serverIp ||
                panel.username != config.username ||
                panel.password != config.password ||
                panel.port != config.port ||
                panel.destinationFolder != config.destinationFolder ||
                panel.isEnabled != config.isEnabled
    }
    
    override fun apply() {
        val panel = configPanel ?: return
        config.serverIp = panel.serverIp
        config.username = panel.username
        config.password = panel.password
        config.port = panel.port
        config.destinationFolder = panel.destinationFolder
        config.isEnabled = panel.isEnabled
    }
    
    override fun reset() {
        val panel = configPanel ?: return
        panel.serverIp = config.serverIp
        panel.username = config.username
        panel.password = config.password
        panel.port = config.port
        panel.destinationFolder = config.destinationFolder
        panel.isEnabled = config.isEnabled
    }
    
    override fun disposeUIResources() {
        configPanel = null
    }
}