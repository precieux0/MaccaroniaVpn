@Composable
fun ConfigurationScreen() {
    var config by remember { mutableStateOf(VPNConfigManager.getCurrentConfig(LocalContext.current)) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Bannière SSH
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SSH : Utilisez votre propre serveur pour une connexion personnalisée",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section Protocole
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Protocole de Connexion",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sélection du protocole
                ProtocolSelector(config, onConfigChange = { config = it })
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section Configuration SSH (toujours visible mais désactivable)
        SSHConfigSection(config, onConfigChange = { config = it })
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Boutons d'action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // Réinitialiser la config SSH
                    config = config.copy(sshConfig = SSHConfig())
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Réinitialiser")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        VPNConfigManager.saveConfig(context, config)
                        // Retour à l'écran principal
                        (context as? ComponentActivity)?.finish()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = config.sshConfig.enableSSH && 
                    config.sshConfig.sshHost.isNotEmpty() &&
                    config.sshConfig.sshUsername.isNotEmpty() &&
                    (config.sshConfig.sshPassword.isNotEmpty() || config.sshConfig.sshPrivateKey.isNotEmpty())
            ) {
                Text("Sauvegarder")
            }
        }
    }
}

@Composable
fun ProtocolSelector(config: VPNConfig, onConfigChange: (VPNConfig) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Protocole",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when (config.protocol) {
                    ProtocolType.SSH -> "SSH (Votre serveur)"
                    ProtocolType.CUSTOM_UDP -> "UDP Custom"
                    ProtocolType.V2RAY -> "V2Ray"
                    ProtocolType.SOCKS5 -> "SOCKS5"
                    else -> "HTTP"
                },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("SSH (Votre serveur)") },
                    onClick = {
                        onConfigChange(config.copy(protocol = ProtocolType.SSH))
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("UDP Custom") },
                    onClick = {
                        onConfigChange(config.copy(protocol = ProtocolType.CUSTOM_UDP))
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("V2Ray") },
                    onClick = {
                        onConfigChange(config.copy(protocol = ProtocolType.V2RAY))
                        expanded = false
                    }
                )
            }
        }
        
        // Description du protocole
        Text(
            text = when (config.protocol) {
                ProtocolType.SSH -> "Utilisez votre propre serveur SSH pour un tunnel sécurisé"
                ProtocolType.CUSTOM_UDP -> "Protocole UDP personnalisé pour performances optimales"
                ProtocolType.V2Ray -> "Protocole avancé pour contournement de censure"
                else -> "Protocole standard"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}