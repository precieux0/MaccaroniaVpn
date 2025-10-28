package com.maccaronia.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.maccaronia.vpn.model.ProtocolType
import com.maccaronia.vpn.protocol.SSHTunnel
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

class VPNService : VpnService() {
    
    companion object {
        const val ACTION_CONNECT = "com.maccaronia.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.maccaronia.vpn.DISCONNECT"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "maccaronia_vpn"
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    // NOUVEAU: Tunnel SSH
    private val sshTunnel = SSHTunnel()
    private var currentConfig: VPNConfig? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = intent.getSerializableExtra("config") as? VPNConfig
                connect(config)
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }
    
    private fun connect(config: VPNConfig? = null) {
        scope.launch {
            try {
                currentConfig = config ?: VPNConfigManager.getCurrentConfig(this@VPNService)
                
                // Démarrer le tunnel SSH si configuré
                if (currentConfig?.protocol == ProtocolType.SSH && 
                    currentConfig?.sshConfig?.enableSSH == true) {
                    startSSHTunnel()
                }
                
                // Configuration du service VPN
                val builder = Builder()
                    .setSession("Maccaronia VPN")
                    .addAddress("10.0.0.2", 32)
                    .addDnsServer("8.8.8.8")
                    .addRoute("0.0.0.0", 0)
                    .setMtu(1500)
                
                vpnInterface = builder.establish()
                startForegroundService()
                isRunning = true
                
                // Démarrer le traitement des paquets
                processPackets()
                
            } catch (e: Exception) {
                e.printStackTrace()
                disconnect()
            }
        }
    }
    
    private suspend fun startSSHTunnel() {
        currentConfig?.sshConfig?.let { sshConfig ->
            if (sshConfig.enableSSH && sshConfig.sshHost.isNotEmpty()) {
                val success = sshTunnel.startSSHTunnel(sshConfig)
                if (!success) {
                    throw Exception("Échec du démarrage du tunnel SSH")
                }
            }
        }
    }
    
    private fun processPackets() {
        scope.launch {
            val input = FileInputStream(vpnInterface!!.fileDescriptor)
            val output = FileOutputStream(vpnInterface!!.fileDescriptor)
            
            val buffer = ByteArray(32767)
            while (isRunning) {
                val length = input.read(buffer)
                if (length > 0) {
                    // Routage selon le protocole
                    when (currentConfig?.protocol) {
                        ProtocolType.SSH -> processSSHTraffic(buffer, length, output)
                        ProtocolType.CUSTOM_UDP -> processCustomUDP(buffer, length, output)
                        ProtocolType.V2RAY -> v2rayProcessPacket(buffer, length, output)
                        else -> output.write(buffer, 0, length)
                    }
                }
            }
        }
    }
    
    private suspend fun processSSHTraffic(packet: ByteArray, length: Int, output: FileOutputStream) {
        try {
            // Si le tunnel SSH est actif, router le trafic via le proxy local
            if (sshTunnel.isTunnelActive()) {
                // Le trafic passe automatiquement par le proxy SOCKS SSH
                output.write(packet, 0, length)
            } else {
                Log.e(TAG, "Tunnel SSH non actif")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur traitement trafic SSH", e)
        }
    }
    
    // ... reste du code existant ...
    
    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }
}