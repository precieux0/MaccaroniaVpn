package com.maccaronia.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maccaronia.vpn.ui.theme.MaccaroniaVPNTheme

class FAQActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaccaroniaVPNTheme {
                FAQScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun FAQScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FAQ & À Propos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Section À Propos du Développeur
                DeveloperSection()
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                // Section Technique
                TechnicalSection()
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            items(getFAQItems()) { item ->
                FAQItem(faqItem = item)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DeveloperSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Développeur",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "À Propos du Développeur",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "okitakoy (Okita Koy Précieux)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Développeur principal de Maccaronia VPN",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Email: okitakoyprecieux@gmail.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Spécialiste en développement Android et technologies réseau. " +
                       "Passionné par la création de solutions de sécurité innovantes " +
                       "et performantes. Maccaronia VPN représente l'aboutissement " +
                       "de plusieurs années d'expertise en développement d'applications " +
                       "VPN avancées.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TechnicalSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Mod.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Technique",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spécifications Techniques",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TechnicalSpecItem("Java Version", "17")
            TechnicalSpecItem("Gradle Artifact", "Version 4")
            TechnicalSpecItem("Protocoles Supportés", "UDP Custom, V2Ray, SNI, DNS")
            TechnicalSpecItem("Format Config", ".maccp (Maccaronia Config)")
            TechnicalSpecItem("Architecture", "MVVM + Coroutines")
            TechnicalSpecItem("UI", "Jetpack Compose Material 3")
        }
    }
}

@Composable
fun TechnicalSpecItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

data class FAQItem(
    val question: String,
    val answer: String,
    val icon: Int = 0
)

@Composable
fun FAQItem(faqItem: FAQItem) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "FAQ",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = faqItem.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faqItem.answer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun getFAQItems(): List<FAQItem> {
    return listOf(
        FAQItem(
            question = "Qu'est-ce que Maccaronia VPN ?",
            answer = "Maccaronia VPN est une application VPN avancée développée par okitakoy, " +
                     "supportant des protocoles personnalisés comme UDP Custom et V2Ray, " +
                     "avec une gestion complète de la configuration via des fichiers .maccp."
        ),
        FAQItem(
            question = "Comment configurer un protocole personnalisé ?",
            answer = "Allez dans l'écran de Configuration → Sélectionnez le protocole " +
                     "(UDP Custom, V2Ray, etc.) → Configurez les paramètres spécifiques " +
                     "→ Sauvegardez la configuration."
        ),
        FAQItem(
            question = "Comment importer/exporter une configuration ?",
            answer = "Utilisez les boutons 'Importer' et 'Exporter Configuration' sur " +
                     "l'écran principal. Les fichiers utilisent l'extension .maccp et " +
                     "contiennent tous les paramètres de votre configuration VPN."
        ),
        FAQItem(
            question = "Quels protocoles sont supportés ?",
            answer = "• UDP Custom (protocole personnalisé)\n" +
                     "• V2Ray (avec configuration avancée)\n" +
                     "• Support SNI (Server Name Indication)\n" +
                     "• DNS personnalisé\n" +
                     "• Payload personnalisable"
        ),
        FAQItem(
            question = "Comment contribuer au projet ?",
            answer = "Le projet est open-source. Vous pouvez forker le repository, " +
                     "proposer des améliorations via des pull requests, ou rapporter " +
                     "des issues sur GitHub. Contact: okitakoyprecieux@gmail.com"
        ),
        FAQItem(
            question = "Qu'est-ce que le format .maccp ?",
            answer = "Le format .maccp (Maccaronia Config Protocol) est un format " +
                     "propriétaire de fichier de configuration qui stocke tous les " +
                     "paramètres VPN dans un fichier JSON chiffré."
        ),
        FAQItem(
            question = "L'application est-elle sécurisée ?",
            answer = "Oui, Maccaronia VPN utilise des standards de sécurité modernes, " +
                     "incluant le chiffrement des configurations, la gestion sécurisée " +
                     "des connexions, et l'isolation des données via le service VPN Android."
        )
    )
}