package com.maccaronia.vpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.maccaronia.vpn.ui.theme.MaccaroniaVPNTheme
import com.maccaronia.vpn.utils.NavigationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private var isVPNRunning by mutableStateOf(false)
    private var vpnStatus by mutableStateOf("Déconnecté")
    private val VPN_REQUEST_CODE = 1001
    private val IMPORT_CONFIG_REQUEST = 1002
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaccaroniaVPNTheme {
                MainScreen(
                    isVPNRunning = isVPNRunning,
                    vpnStatus = vpnStatus,
                    onToggleVPN = { toggleVPN() },
                    onOpenConfig = { openConfigScreen() },
                    onImportConfig = { importConfig() },
                    onExportConfig = { exportConfig() }
                )
            }
        }
        
        // Vérifier l'état du service VPN au démarrage
        checkVPNStatus()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_faq -> {
                NavigationUtils.openFAQActivity(this)
                true
            }
            R.id.menu_about -> {
                NavigationUtils.openAboutActivity(this)
                true
            }
            R.id.menu_settings -> {
                openConfigScreen()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun checkVPNStatus() {
        // Vérifier si le service VPN est en cours d'exécution
        // Cette implémentation devra être adaptée selon votre logique réelle
        isVPNRunning = false
        vpnStatus = "Déconnecté"
    }
    
    private fun toggleVPN() {
        if (isVPNRunning) {
            stopVPN()
        } else {
            startVPN()
        }
    }
    
    private fun startVPN() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            // Demander la permission VPN
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            // Permission déjà accordée, démarrer le VPN
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }
    
    private fun stopVPN() {
        val intent = Intent(this, VPNService::class.java)
        intent.action = VPNService.ACTION_DISCONNECT
        startService(intent)
        isVPNRunning = false
        vpnStatus = "Déconnecté"
    }
    
    private fun openConfigScreen() {
        val intent = Intent(this, ConfigActivity::class.java)
        startActivity(intent)
    }
    
    private fun importConfig() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream"))
        }
        startActivityForResult(intent, IMPORT_CONFIG_REQUEST)
    }
    
    private fun exportConfig() {
        // Implémentation de l'exportation
        val context = LocalContext.current
        lifecycleScope.launch {
            // Simuler l'exportation
            delay(500)
            showToast("Configuration exportée avec succès")
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            VPN_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    // Démarrer le service VPN
                    val intent = Intent(this, VPNService::class.java)
                    intent.action = VPNService.ACTION_CONNECT
                    startService(intent)
                    isVPNRunning = true
                    vpnStatus = "Connecté"
                    showToast("VPN démarré avec succès")
                } else {
                    showToast("Permission VPN refusée")
                }
            }
            
            IMPORT_CONFIG_REQUEST -> {
                if (resultCode == RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // Importer la configuration
                        lifecycleScope.launch {
                            // Simuler l'importation
                            delay(500)
                            showToast("Configuration importée avec succès")
                        }
                    }
                }
            }
        }
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isVPNRunning: Boolean,
    vpnStatus: String,
    onToggleVPN: () -> Unit,
    onOpenConfig: () -> Unit,
    onImportConfig: () -> Unit,
    onExportConfig: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Maccaronia VPN",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo et statut
            VPNStatusSection(isVPNRunning, vpnStatus)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Bouton principal VPN
            VPNToggleButton(
                isVPNRunning = isVPNRunning,
                onToggleVPN = onToggleVPN
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Boutons de configuration
            ConfigurationButtons(
                onOpenConfig = onOpenConfig,
                onImportConfig = onImportConfig,
                onExportConfig = onExportConfig
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Informations supplémentaires
            AdditionalInfo()
        }
    }
}

@Composable
fun VPNStatusSection(isVPNRunning: Boolean, vpnStatus: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icône du VPN avec animation
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Statut VPN",
            modifier = Modifier.size(120.dp),
            tint = if (isVPNRunning) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Statut texte
        Text(
            text = vpnStatus,
            style = MaterialTheme.typography.headlineMedium,
            color = if (isVPNRunning) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Message de statut
        Text(
            text = if (isVPNRunning) "Votre connexion est sécurisée" 
                   else "Prêt à sécuriser votre connexion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VPNToggleButton(isVPNRunning: Boolean, onToggleVPN: () -> Unit) {
    Button(
        onClick = onToggleVPN,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isVPNRunning) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = if (isVPNRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isVPNRunning) "Arrêter" else "Démarrer",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isVPNRunning) "DÉCONNECTER" else "CONNECTER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
fun ConfigurationButtons(
    onOpenConfig: () -> Unit,
    onImportConfig: () -> Unit,
    onExportConfig: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bouton Configuration
        OutlinedButton(
            onClick = onOpenConfig,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuration"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Configuration Avancée",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Boutons Import/Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onImportConfig,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ImportExport,
                    contentDescription = "Importer"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importer")
            }
            
            OutlinedButton(
                onClick = onExportConfig,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ImportExport,
                    contentDescription = "Exporter"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exporter")
            }
        }
    }
}

@Composable
fun AdditionalInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Fonctionnalités Supportées",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FeatureChip("UDP Custom")
            FeatureChip("V2Ray")
            FeatureChip("SNI")
            FeatureChip("DNS")
            FeatureChip("Payload")
        }
    }
}

@Composable
fun FeatureChip(feature: String) {
    AssistChip(
        onClick = { },
        label = { Text(feature) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    )
}

// Extension pour faciliter l'utilisation
fun ComponentActivity.showToast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}