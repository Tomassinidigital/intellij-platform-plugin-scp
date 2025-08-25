package com.scpsync.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Font
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class ScpSyncLogWindow : ToolWindowFactory {
    
    companion object {
        private val instances = mutableMapOf<Project, LogWindowInstance>()
        
        fun getInstance(project: Project): LogWindowInstance {
            return instances.getOrPut(project) { LogWindowInstance() }
        }
    }
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val logInstance = getInstance(project)
        logInstance.toolWindow = toolWindow
        
        val content = ContentFactory.getInstance().createContent(logInstance.createContent(), "SCP Sync Log", false)
        toolWindow.contentManager.addContent(content)
    }
    
    class LogWindowInstance {
        var toolWindow: ToolWindow? = null
        private val logArea = JTextArea()
        private val dateFormat = SimpleDateFormat("HH:mm:ss")
        
        init {
            logArea.isEditable = false
            logArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            logArea.background = java.awt.Color(43, 43, 43)
            logArea.foreground = java.awt.Color(187, 187, 187)
        }
        
        fun createContent(): JPanel {
            val panel = JPanel(BorderLayout())
            val scrollPane = JBScrollPane(logArea)
            panel.add(scrollPane, BorderLayout.CENTER)
            return panel
        }
        
        fun show() {
            toolWindow?.show()
        }
        
        fun addLogEntry(message: String) {
            SwingUtilities.invokeLater {
                val timestamp = dateFormat.format(Date())
                val logEntry = "[$timestamp] $message\n"
                logArea.append(logEntry)
                logArea.caretPosition = logArea.document.length
            }
        }
        
        fun clearLog() {
            SwingUtilities.invokeLater {
                logArea.text = ""
            }
        }
    }
}