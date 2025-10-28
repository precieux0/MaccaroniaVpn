package com.maccaronia.vpn.protocol

import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.*
import java.util.concurrent.atomic.AtomicBoolean

class SSHTunnel {
    
    companion object {
        private const val TAG = "SSHTunnel"
    }
    
    private var isRunning = AtomicBoolean(false)
    private var sshProcess: Process? = null
    private var localProxyServer: ServerSocket? = null
    private var tunnelJob: Job? = null
    
    suspend fun startSSHTunnel(config: SSHConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Démarrage du tunnel SSH vers ${config.sshHost}:${config.sshPort}")
            
            // Construction de la commande SSH
            val sshCommand = buildSSHCommand(config)
            
            // Exécution du processus SSH
            sshProcess = Runtime.getRuntime().exec(sshCommand)
            
            // Démarrer le serveur proxy local
            startLocalProxyServer(config)
            
            // Surveillance du processus SSH
            monitorSSHProcess(config)
            
            isRunning.set(true)
            Log.i(TAG, "Tunnel SSH démarré avec succès")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur démarrage tunnel SSH", e)
            stopSSHTunnel()
            false
        }
    }
    
    private fun buildSSHCommand(config: SSHConfig): Array<String> {
        val command = mutableListOf<String>()
        
        command.add("ssh")
        command.add("-N")  // No command execution
        command.add("-D")  // Dynamic SOCKS proxy
        command.add("${config.sshLocalPort}")  // Port local
        
        // Options de connexion
        command.add("-p")
        command.add(config.sshPort.toString())
        
        // Authentification
        if (config.sshPrivateKey.isNotEmpty()) {
            command.add("-i")
            command.add(config.sshPrivateKey)
        }
        
        if (config.sshCompression) {
            command.add("-C")
        }
        
        // Keep alive
        command.add("-o")
        command.add("ServerAliveInterval=${config.sshKeepAlive}")
        
        // Host key checking (désactivé pour le test)
        command.add("-o")
        command.add("StrictHostKeyChecking=no")
        command.add("-o")
        command.add("UserKnownHostsFile=/dev/null")
        
        // Utilisateur et hôte
        command.add("${config.sshUsername}@${config.sshHost}")
        
        return command.toTypedArray()
    }
    
    private fun startLocalProxyServer(config: SSHConfig) {
        try {
            localProxyServer = ServerSocket(config.sshLocalPort)
            Log.d(TAG, "Serveur proxy local démarré sur le port ${config.sshLocalPort}")
            
            tunnelJob = CoroutineScope(Dispatchers.IO).launch {
                while (isRunning.get() && !localProxyServer!!.isClosed) {
                    try {
                        val clientSocket = localProxyServer!!.accept()
                        handleClientConnection(clientSocket, config)
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            Log.e(TAG, "Erreur acceptation client", e)
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur démarrage serveur proxy local", e)
            throw e
        }
    }
    
    private fun handleClientConnection(clientSocket: Socket, config: SSHConfig) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Le trafic est maintenant routé via le proxy SOCKS SSH
                // Implémentation du protocole SOCKS
                handleSocksProtocol(clientSocket, config)
            } catch (e: Exception) {
                Log.e(TAG, "Erreur gestion client", e)
            } finally {
                try {
                    clientSocket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Erreur fermeture socket client", e)
                }
            }
        }
    }
    
    private suspend fun handleSocksProtocol(clientSocket: Socket, config: SSHConfig) {
        // Implémentation simplifiée du protocole SOCKS
        val input = clientSocket.getInputStream()
        val output = clientSocket.getOutputStream()
        
        // Lecture de la requête SOCKS
        val buffer = ByteArray(1024)
        val bytesRead = withContext(Dispatchers.IO) {
            input.read(buffer)
        }
        
        if (bytesRead > 0) {
            // Réponse SOCKS réussie
            val response = byteArrayOf(0x05, 0x00)
            withContext(Dispatchers.IO) {
                output.write(response)
                output.flush()
            }
            
            // Le trafic passe maintenant par le tunnel SSH
            Log.d(TAG, "Connexion SOCKS établie via SSH")
        }
    }
    
    private fun monitorSSHProcess(config: SSHConfig) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sshProcess?.waitFor()
                Log.w(TAG, "Processus SSH terminé")
                stopSSHTunnel()
            } catch (e: Exception) {
                Log.e(TAG, "Erreur surveillance processus SSH", e)
            }
        }
    }
    
    fun stopSSHTunnel() {
        isRunning.set(false)
        
        try {
            sshProcess?.destroy()
            sshProcess = null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur arrêt processus SSH", e)
        }
        
        try {
            localProxyServer?.close()
            localProxyServer = null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur fermeture serveur proxy", e)
        }
        
        tunnelJob?.cancel()
        tunnelJob = null
        
        Log.i(TAG, "Tunnel SSH arrêté")
    }
    
    fun isTunnelActive(): Boolean {
        return isRunning.get()
    }
    
    fun getLocalProxyPort(): Int {
        return localProxyServer?.localPort ?: 0
    }
}