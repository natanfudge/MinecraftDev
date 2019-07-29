/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.mcp

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.i18n.I18nFileListener
import com.demonwav.mcdev.platform.AbstractModule
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.mcp.srg.SrgManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.util.messages.MessageBusConnection
import javax.swing.Icon

class McpModule(facet: MinecraftFacet) : AbstractModule(facet) {

    private lateinit var connection: MessageBusConnection

    private val settings: McpModuleSettings = McpModuleSettings.getInstance(module)
    val accessTransformers = mutableSetOf<VirtualFile>()

    var srgManager: SrgManager? = null
        private set

    override fun init() {
        val files = getSettings().mappingFiles
        if (!files.isEmpty()) {
            srgManager = SrgManager.getInstance(files)
            srgManager?.parse()
        }

        connection = project.messageBus.connect()
        connection.subscribe(VirtualFileManager.VFS_CHANGES, I18nFileListener)
    }

    override val moduleType = McpModuleType
    override val type = PlatformType.MCP
    override val icon: Icon? = null

    override fun writeErrorMessageForEventParameter(eventClass: PsiClass, method: PsiMethod) = ""

    fun getSettings() = settings.state

    fun updateSettings(data: McpModuleSettings.State) {
        this.settings.loadState(data)
        srgManager = SrgManager.getInstance(data.mappingFiles)
        srgManager?.parse()
    }

    fun addAccessTransformerFile(file: VirtualFile) {
        accessTransformers.add(file)
    }

    override fun dispose() {
        super.dispose()

        connection.disconnect()
        accessTransformers.clear()
        srgManager = null
    }
}
