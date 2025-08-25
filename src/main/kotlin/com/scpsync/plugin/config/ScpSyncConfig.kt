package com.scpsync.plugin.config

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.APPLICATION)
@State(
    name = "ScpSyncConfig",
    storages = [Storage("scpSyncConfig.xml")]
)
class ScpSyncConfig : PersistentStateComponent<ScpSyncConfig> {
    
    var serverIp: String = ""
    var username: String = ""
    var password: String = ""
    var port: Int = 22
    var destinationFolder: String = ""
    var isEnabled: Boolean = false
    
    override fun getState(): ScpSyncConfig = this
    
    override fun loadState(state: ScpSyncConfig) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        fun getInstance(): ScpSyncConfig = service()
    }
}