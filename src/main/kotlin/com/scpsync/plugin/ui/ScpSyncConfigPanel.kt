package com.scpsync.plugin.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.scpsync.plugin.services.ScpSyncService
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class ScpSyncConfigPanel : JPanel(BorderLayout()) {
    
    private val serverIpField = JBTextField()
    private val usernameField = JBTextField()
    private val passwordField = JBPasswordField()
    private val portField = JBTextField()
    private val destinationFolderField = JBTextField()
    private val enabledCheckBox = JBCheckBox("Enable SCP Synchronization")
    
    private val testConnectionButton = JButton("Test Connection")
    private val testTransferButton = JButton("Test File Transfer")
    
    init {
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        portField.text = "22"
        
        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckBox)
            .addSeparator()
            .addLabeledComponent(JBLabel("Server IP:"), serverIpField, 1, false)
            .addLabeledComponent(JBLabel("Username:"), usernameField, 1, false)
            .addLabeledComponent(JBLabel("Password:"), passwordField, 1, false)
            .addLabeledComponent(JBLabel("Port:"), portField, 1, false)
            .addLabeledComponent(JBLabel("Destination Folder:"), destinationFolderField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonPanel.add(testConnectionButton)
        buttonPanel.add(testTransferButton)
        
        add(formPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }
    
    private fun setupListeners() {
        testConnectionButton.addActionListener {
            testConnection()
        }
        
        testTransferButton.addActionListener {
            testFileTransfer()
        }
    }
    
    private fun testConnection() {
        val service = ApplicationManager.getApplication().service<ScpSyncService>()
        
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = service.testConnection(
                serverIp, username, String(passwordField.password), port
            )
            
            SwingUtilities.invokeLater {
                if (result.isSuccess) {
                    Messages.showInfoMessage(
                        "Connection successful!",
                        "Test Connection"
                    )
                } else {
                    Messages.showErrorDialog(
                        "Connection failed: ${result.errorMessage}",
                        "Test Connection"
                    )
                }
            }
        }
    }
    
    private fun testFileTransfer() {
        val service = ApplicationManager.getApplication().service<ScpSyncService>()
        
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = service.testFileTransfer(
                serverIp, username, String(passwordField.password), port, destinationFolder
            )
            
            SwingUtilities.invokeLater {
                if (result.isSuccess) {
                    Messages.showInfoMessage(
                        "File transfer test successful!",
                        "Test File Transfer"
                    )
                } else {
                    Messages.showErrorDialog(
                        "File transfer test failed: ${result.errorMessage}",
                        "Test File Transfer"
                    )
                }
            }
        }
    }
    
    var serverIp: String
        get() = serverIpField.text.trim()
        set(value) { serverIpField.text = value }
    
    var username: String
        get() = usernameField.text.trim()
        set(value) { usernameField.text = value }
    
    var password: String
        get() = String(passwordField.password)
        set(value) { passwordField.text = value }
    
    var port: Int
        get() = portField.text.toIntOrNull() ?: 22
        set(value) { portField.text = value.toString() }
    
    var destinationFolder: String
        get() = destinationFolderField.text.trim()
        set(value) { destinationFolderField.text = value }
    
    var isEnabled: Boolean
        get() = enabledCheckBox.isSelected
        set(value) { enabledCheckBox.isSelected = value }
}