package com.maccaronia.vpn.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.maccaronia.vpn.model.SSHTunnelMethod
import com.maccaronia.vpn.model.VPNConfig

@Composable
fun SSHConfigSection(config: VPNConfig, onConfigChange: (VPNConfig) -> Unit) {
    var showAdvanced by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête avec toggle SSH
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "SSH",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tunnel SSH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "Configurez votre propre serveur SSH",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Switch(
                    checked = config.sshConfig.enableSSH,
                    onCheckedChange = { enabled ->
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(enableSSH = enabled)
                            )
                        )
                    }
                )
            }
            
            // Configuration SSH (visible seulement si activé)
            if (config.sshConfig.enableSSH) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Section Connexion de Base
                SSHConnectionSection(config, onConfigChange)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Section Authentification
                SSHAuthSection(config, onConfigChange)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bouton Options Avancées
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Options Avancées",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    IconButton(
                        onClick = { showAdvanced = !showAdvanced }
                    ) {
                        Icon(
                            imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showAdvanced) "Cacher" else "Afficher"
                        )
                    }
                }
                
                // Options Avancées
                if (showAdvanced) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SSHAdvancedOptions(config, onConfigChange)
                }
                
                // Indicateur de Configuration
                SSHConfigStatus(config)
            }
        }
    }
}

@Composable
fun SSHConnectionSection(config: VPNConfig, onConfigChange: (VPNConfig) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Connexion Serveur",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Hôte et Port
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = config.sshConfig.sshHost,
                    onValueChange = { host ->
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(sshHost = host)
                            )
                        )
                    },
                    label = { Text("Adresse du serveur*") },
                    placeholder = { Text("ex: mon-serveur.ddns.net") },
                    modifier = Modifier.weight(2f),
                    leadingIcon = {
                        Icon(Icons.Default.Dns, contentDescription = "Hôte")
                    },
                    singleLine = true,
                    isError = config.sshConfig.sshHost.isEmpty()
                )
                
                OutlinedTextField(
                    value = config.sshConfig.sshPort.toString(),
                    onValueChange = { port ->
                        val portNum = port.toIntOrNull() ?: 22
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(sshPort = portNum)
                            )
                        )
                    },
                    label = { Text("Port*") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Méthode de Tunnel
            SSHMethodSelector(config, onConfigChange)
        }
    }
}

@Composable
fun SSHAuthSection(config: VPNConfig, onConfigChange: (VPNConfig) -> Unit) {
    var authMethod by remember { mutableStateOf(if (config.sshConfig.sshPrivateKey.isNotEmpty()) "key" else "password") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Authentification",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Sélection méthode d'authentification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = authMethod == "password",
                        onClick = { 
                            authMethod = "password"
                            onConfigChange(config.copy(sshConfig = config.sshConfig.copy(sshPrivateKey = "")))
                        }
                    )
                    Text("Mot de passe", modifier = Modifier.padding(start = 8.dp))
                }
                
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = authMethod == "key",
                        onClick = { authMethod = "key" }
                    )
                    Text("Clé privée", modifier = Modifier.padding(start = 8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Champ utilisateur (commun aux deux méthodes)
            OutlinedTextField(
                value = config.sshConfig.sshUsername,
                onValueChange = { username ->
                    onConfigChange(
                        config.copy(
                            sshConfig = config.sshConfig.copy(sshUsername = username)
                        )
                    )
                },
                label = { Text("Nom d'utilisateur*") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Utilisateur")
                },
                singleLine = true,
                isError = config.sshConfig.sshUsername.isEmpty()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Champs spécifiques à la méthode
            when (authMethod) {
                "password" -> {
                    OutlinedTextField(
                        value = config.sshConfig.sshPassword,
                        onValueChange = { password ->
                            onConfigChange(
                                config.copy(
                                    sshConfig = config.sshConfig.copy(sshPassword = password)
                                )
                            )
                        },
                        label = { Text("Mot de passe*") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Mot de passe")
                        },
                        singleLine = true,
                        isError = config.sshConfig.sshPassword.isEmpty()
                    )
                }
                "key" -> {
                    OutlinedTextField(
                        value = config.sshConfig.sshPrivateKey,
                        onValueChange = { privateKey ->
                            onConfigChange(
                                config.copy(
                                    sshConfig = config.sshConfig.copy(sshPrivateKey = privateKey)
                                )
                            )
                        },
                        label = { Text("Clé privée SSH*") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("-----BEGIN PRIVATE KEY-----...") },
                        maxLines = 4,
                        isError = config.sshConfig.sshPrivateKey.isEmpty()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = config.sshConfig.sshKeyPassphrase,
                        onValueChange = { passphrase ->
                            onConfigChange(
                                config.copy(
                                    sshConfig = config.sshConfig.copy(sshKeyPassphrase = passphrase)
                                )
                            )
                        },
                        label = { Text("Passphrase (optionnel)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun SSHMethodSelector(config: VPNConfig, onConfigChange: (VPNConfig) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Type de tunnel",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when (config.sshConfig.sshTunnelMethod) {
                    SSHTunnelMethod.DYNAMIC -> "SOCKS Dynamique (Recommandé)"
                    SSHTunnelMethod.DIRECT -> "Tunnel Direct"
                    SSHTunnelMethod.LOCAL_FORWARDING -> "Port Forwarding Local"
                    SSHTunnelMethod.REMOTE_FORWARDING -> "Port Forwarding Distant"
                },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                leadingIcon = {
                    Icon(Icons.Default.Tunnel, contentDescription = "Méthode tunnel")
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("SOCKS Dynamique (Recommandé)") },
                    onClick = {
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(
                                    sshTunnelMethod = SSHTunnelMethod.DYNAMIC,
                                    sshLocalPort = 1080
                                )
                            )
                        )
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Tunnel Direct") },
                    onClick = {
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(sshTunnelMethod = SSHTunnelMethod.DIRECT)
                            )
                        )
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Port Forwarding Local") },
                    onClick = {
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(
                                    sshTunnelMethod = SSHTunnelMethod.LOCAL_FORWARDING,
                                    sshLocalPort = 8080
                                )
                            )
                        )
                        expanded = false
                    }
                )
            }
        }
        
        // Description de la méthode sélectionnée
        Text(
            text = when (config.sshConfig.sshTunnelMethod) {
                SSHTunnelMethod.DYNAMIC -> "Crée un proxy SOCKS local. Compatible avec la plupart des applications."
                SSHTunnelMethod.DIRECT -> "Tunnel SSH direct. Plus rapide mais moins compatible."
                SSHTunnelMethod.LOCAL_FORWARDING -> "Redirige un port local vers le serveur distant."
                SSHTunnelMethod.REMOTE_FORWARDING -> "Redirige un port distant vers votre machine locale."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SSHAdvancedOptions(config: VPNConfig, onConfigChange: (VPNConfig) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Paramètres Avancés",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Port Local
            OutlinedTextField(
                value = config.sshConfig.sshLocalPort.toString(),
                onValueChange = { port ->
                    onConfigChange(
                        config.copy(
                            sshConfig = config.sshConfig.copy(
                                sshLocalPort = port.toIntOrNull() ?: 1080
                            )
                        )
                    )
                },
                label = { Text("Port Local") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(Icons.Default.Numbers, contentDescription = "Port local")
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Options diverses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = config.sshConfig.sshCompression,
                            onCheckedChange = { compression ->
                                onConfigChange(
                                    config.copy(
                                        sshConfig = config.sshConfig.copy(sshCompression = compression)
                                    )
                                )
                            }
                        )
                        Text("Compression", modifier = Modifier.padding(start = 8.dp))
                    }
                    Text(
                        "Réduit l'utilisation données",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                OutlinedTextField(
                    value = config.sshConfig.sshKeepAlive.toString(),
                    onValueChange = { keepAlive ->
                        onConfigChange(
                            config.copy(
                                sshConfig = config.sshConfig.copy(
                                    sshKeepAlive = keepAlive.toIntOrNull() ?: 60
                                )
                            )
                        )
                    },
                    label = { Text("Keep Alive (s)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

@Composable
fun SSHConfigStatus(config: VPNConfig) {
    val errors = mutableListOf<String>()
    
    // Validation de la configuration
    if (config.sshConfig.sshHost.isEmpty()) errors.add("Adresse serveur requise")
    if (config.sshConfig.sshUsername.isEmpty()) errors.add("Nom d'utilisateur requis")
    if (config.sshConfig.sshPassword.isEmpty() && config.sshConfig.sshPrivateKey.isEmpty()) {
        errors.add("Mot de passe ou clé privée requis")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (errors.isEmpty()) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (errors.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Statut",
                    tint = if (errors.isEmpty()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (errors.isEmpty()) "Configuration SSH valide ✓" else "Configuration incomplète",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
            
            if (errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    errors.forEach { error ->
                        Text(
                            text = "• $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}