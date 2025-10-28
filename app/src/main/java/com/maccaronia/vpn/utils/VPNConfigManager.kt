package com.maccaronia.vpn.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.maccaronia.vpn.model.VPNConfig
import java.io.*

object VPNConfigManager {
    
    private const val CONFIG_EXTENSION = ".maccp"
    private const TAG = "VPNConfigManager"
    
    fun exportConfig(context: Context, config: VPNConfig): Boolean {
        return try {
            val content = config.toJson()
            val fileName = "maccaronia_config_${System.currentTimeMillis()}$CONFIG_EXTENSION"
            
            // Créer un fichier dans le stockage externe
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(content)
            
            Log.d(TAG, "Configuration exportée: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur export config", e)
            false
        }
    }
    
    fun importConfig(context: Context, uri: Uri): VPNConfig? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val content = inputStream.bufferedReader().use { it.readText() }
                VPNConfig.fromJson(content)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur import config", e)
            null
        }
    }
    
    fun saveConfig(context: Context, config: VPNConfig) {
        val sharedPref = context.getSharedPreferences("vpn_config", Context.MODE_PRIVATE)
        sharedPref.edit().putString("current_config", config.toJson()).apply()
    }
    
    fun getCurrentConfig(context: Context): VPNConfig {
        val sharedPref = context.getSharedPreferences("vpn_config", Context.MODE_PRIVATE)
        val configJson = sharedPref.getString("current_config", null)
        return configJson?.let { VPNConfig.fromJson(it) } ?: VPNConfig()
    }
}