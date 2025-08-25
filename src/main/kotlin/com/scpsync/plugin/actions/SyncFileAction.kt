package com.scpsync.plugin.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.scpsync.plugin.config.ScpSyncConfig
import com.scpsync.plugin.services.ScpSyncService
import com.scpsync.plugin.ui.ScpSyncLogWindow

class SyncFileAction : AnAction("Sincronizza in remoto") {
    
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = e.project
        val config = ScpSyncConfig.getInstance()
        
        e.presentation.isEnabledAndVisible = file != null && 
                                           project != null && 
                                           !file.isDirectory && 
                                           config.isEnabled
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = e.project ?: return
        
        val logWindow = ScpSyncLogWindow.getInstance(project)
        logWindow.show()
        logWindow.clearLog()
        logWindow.addLogEntry("Starting synchronization for: ${file.name}")
        
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Synchronizing file to remote server", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Synchronizing ${file.name}..."
                indicator.isIndeterminate = true
                
                val service = ApplicationManager.getApplication().service<ScpSyncService>()
                val result = service.syncFile(project, file)
                
                ApplicationManager.getApplication().invokeLater {
                    if (result.isSuccess) {
                        logWindow.addLogEntry("✓ Synchronization completed successfully: ${result.message}")
                        Messages.showInfoMessage(
                            project,
                            "File '${file.name}' synchronized successfully!",
                            "SCP Sync"
                        )
                    } else {
                        logWindow.addLogEntry("✗ Synchronization failed: ${result.errorMessage}")
                        Messages.showErrorDialog(
                            project,
                            "Failed to synchronize '${file.name}': ${result.errorMessage}",
                            "SCP Sync Error"
                        )
                    }
                }
            }
        })
    }
}