package com.maccaronia.vpn.model

import com.google.gson.Gson
import java.io.Serializable

data class VPNConfig(
    var protocol: ProtocolType = ProtocolType.CUSTOM_UDP,
    var serverAddress: String = "",
    var serverPort: Int = 0,
    var customUDPConfig: CustomUDPConfig = CustomUDPConfig(),
    var v2rayConfig: V2RayConfig = V2RayConfig(),
    var sshConfig: SSHConfig = SSHConfig(),  // NOUVEAU
    var dnsConfig: DNSConfig = DNSConfig(),
    var sniConfig: SNIConfig = SNIConfig(),
    var payloadConfig: PayloadConfig = PayloadConfig()
) : Serializable {
    
    fun toJson(): String {
        return Gson().toJson(this)
    }
    
    companion object {
        fun fromJson(json: String): VPNConfig {
            return Gson().fromJson(json, VPNConfig::class.java)
        }
    }
}

enum class ProtocolType {
    CUSTOM_UDP, V2RAY, SSH, SOCKS5, HTTP  // SSH ajouté
}

// NOUVEAU: Configuration SSH
data class SSHConfig(
    var enableSSH: Boolean = false,
    var sshHost: String = "",
    var sshPort: Int = 22,
    var sshUsername: String = "",
    var sshPassword: String = "",
    var sshPrivateKey: String = "",
    var sshKeyPassphrase: String = "",
    var sshTunnelMethod: SSHTunnelMethod = SSHTunnelMethod.DYNAMIC,
    var sshLocalPort: Int = 1080,
    var sshRemotePort: Int = 8080,
    var sshCompression: Boolean = true,
    var sshKeepAlive: Int = 60
) : Serializable

enum class SSHTunnelMethod {
    DIRECT,           // Tunnel direct
    DYNAMIC,          // SOCKS dynamique
    LOCAL_FORWARDING, // Port forwarding local
    REMOTE_FORWARDING // Port forwarding distant
}

// Configurations existantes...
data class CustomUDPConfig(
    var encryptionKey: String = "",
    var packetSize: Int = 1400,
    var timeout: Int = 5000
)

data class V2RayConfig(
    var inboundTag: String = "proxy",
    var outboundTag: String = "direct",
    var routingRules: String = ""
)

data class DNSConfig(
    var primaryDNS: String = "8.8.8.8",
    var secondaryDNS: String = "1.1.1.1",
    var dohServer: String = ""
)

data class SNIConfig(
    var hostname: String = "",
    var enableSNI: Boolean = true
)

data class PayloadConfig(
    var customPayload: String = "",
    var enablePayload: Boolean = false
)