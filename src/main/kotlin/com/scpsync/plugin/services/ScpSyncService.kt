package com.scpsync.plugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.scpsync.plugin.config.ScpSyncConfig
import com.scpsync.plugin.utils.ScpResult
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service(Service.Level.APPLICATION)
class ScpSyncService {
    
    private val logger = thisLogger()
    private val config = ScpSyncConfig.getInstance()
    
    fun syncFile(project: Project, file: VirtualFile): ScpResult {
        if (!config.isEnabled) {
            return ScpResult.failure("SCP synchronization is disabled")
        }
        
        if (config.serverIp.isEmpty() || config.username.isEmpty()) {
            return ScpResult.failure("Server configuration is incomplete")
        }
        
        try {
            logger.info("Starting sync for file: ${file.path}")
            
            // Create temp copy
            val tempFile = createTempCopy(file)
            logger.info("Created temp copy: ${tempFile.absolutePath}")
            
            // Calculate destination path
            val destinationPath = calculateDestinationPath(project, file)
            logger.info("Destination path: $destinationPath")
            
            // Execute SCP command
            val result = executeScp(tempFile, destinationPath)
            
            // Cleanup temp file
            tempFile.delete()
            logger.info("Cleaned up temp file")
            
            return result
            
        } catch (e: Exception) {
            logger.error("Error during file sync", e)
            return ScpResult.failure("Sync failed: ${e.message}")
        }
    }
    
    private fun createTempCopy(file: VirtualFile): File {
        val tempDir = System.getProperty("java.io.tmpdir")
        val tempFile = File(tempDir, "scp_sync_${System.currentTimeMillis()}_${file.name}")
        
        Files.copy(Paths.get(file.path), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        
        return tempFile
    }
    
    private fun calculateDestinationPath(project: Project, file: VirtualFile): String {
        val projectBasePath = project.basePath ?: ""
        val filePath = file.path
        
        // Calculate relative path from project root
        val relativePath = if (filePath.startsWith(projectBasePath)) {
            filePath.substring(projectBasePath.length).trimStart('/', '\\')
        } else {
            file.name
        }
        
        // Combine with destination folder
        val destinationBase = config.destinationFolder.trimEnd('/', '\\')
        return "$destinationBase/$relativePath".replace('\\', '/')
    }
    
    private fun executeScp(sourceFile: File, destinationPath: String): ScpResult {
        try {
            val command = buildScpCommand(sourceFile.absolutePath, destinationPath)
            logger.info("Executing SCP command: ${command.joinToString(" ") { if (it.contains("sshpass")) "sshpass -p ***" else it }}")
            
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            logger.info("SCP command output: $output")
            logger.info("SCP command exit code: $exitCode")
            
            return if (exitCode == 0) {
                ScpResult.success("File synchronized successfully")
            } else {
                ScpResult.failure("SCP command failed with exit code $exitCode: $output")
            }
            
        } catch (e: Exception) {
            logger.error("Error executing SCP command", e)
            return ScpResult.failure("SCP execution failed: ${e.message}")
        }
    }
    
    private fun buildScpCommand(sourcePath: String, destinationPath: String): List<String> {
        val command = mutableListOf<String>()
        
        // Use sshpass for password authentication (if available)
        if (config.password.isNotEmpty()) {
            command.addAll(listOf("sshpass", "-p", config.password))
        }
        
        command.addAll(listOf(
            "scp",
            "-P", config.port.toString(),
            "-o", "StrictHostKeyChecking=no",
            "-o", "UserKnownHostsFile=/dev/null",
            sourcePath,
            "${config.username}@${config.serverIp}:$destinationPath"
        ))
        
        return command
    }
    
    fun testConnection(serverIp: String, username: String, password: String, port: Int): ScpResult {
        try {
            logger.info("Testing connection to $username@$serverIp:$port")
            
            val command = mutableListOf<String>()
            if (password.isNotEmpty()) {
                command.addAll(listOf("sshpass", "-p", password))
            }
            
            command.addAll(listOf(
                "ssh",
                "-p", port.toString(),
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=/dev/null",
                "-o", "ConnectTimeout=10",
                "$username@$serverIp",
                "echo 'Connection test successful'"
            ))
            
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            logger.info("Connection test output: $output")
            logger.info("Connection test exit code: $exitCode")
            
            return if (exitCode == 0) {
                ScpResult.success("Connection successful")
            } else {
                ScpResult.failure("Connection failed: $output")
            }
            
        } catch (e: Exception) {
            logger.error("Error testing connection", e)
            return ScpResult.failure("Connection test failed: ${e.message}")
        }
    }
    
    fun testFileTransfer(serverIp: String, username: String, password: String, port: Int, destinationFolder: String): ScpResult {
        try {
            logger.info("Testing file transfer to $username@$serverIp:$port")
            
            // Create a test file
            val tempDir = System.getProperty("java.io.tmpdir")
            val testFile = File(tempDir, "scp_test_${System.currentTimeMillis()}.txt")
            testFile.writeText("SCP test file created at ${System.currentTimeMillis()}")
            
            val destinationPath = "${destinationFolder.trimEnd('/', '\\')}/scp_test_${System.currentTimeMillis()}.txt"
            
            val command = mutableListOf<String>()
            if (password.isNotEmpty()) {
                command.addAll(listOf("sshpass", "-p", password))
            }
            
            command.addAll(listOf(
                "scp",
                "-P", port.toString(),
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=/dev/null",
                testFile.absolutePath,
                "$username@$serverIp:$destinationPath"
            ))
            
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            // Cleanup test file
            testFile.delete()
            
            logger.info("File transfer test output: $output")
            logger.info("File transfer test exit code: $exitCode")
            
            return if (exitCode == 0) {
                ScpResult.success("File transfer test successful")
            } else {
                ScpResult.failure("File transfer test failed: $output")
            }
            
        } catch (e: Exception) {
            logger.error("Error testing file transfer", e)
            return ScpResult.failure("File transfer test failed: ${e.message}")
        }
    }
}