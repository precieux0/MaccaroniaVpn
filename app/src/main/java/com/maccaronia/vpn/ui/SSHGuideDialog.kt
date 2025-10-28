@Composable
fun SSHGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guide Configuration SSH") },
        text = {
            Column {
                Text("Pour utiliser SSH, vous avez besoin :", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• D'un serveur SSH accessible depuis Internet")
                Text("• Des identifiants de connexion (user/mot de passe ou clé)")
                Spacer(modifier = Modifier.height(12.dp))
                Text("Serveurs recommandés :", fontWeight = FontWeight.Bold)
                Text("• Raspberry Pi à domicile")
                Text("• VPS (DigitalOcean, OVH, etc.)")
                Text("• Serveur cloud (AWS, Google Cloud, etc.)")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Compris")
            }
        }
    )
}