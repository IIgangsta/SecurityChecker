package com.example.securitychecker.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

@Composable
fun CheckCard(result: CheckResult, isIgnored: Boolean, onFix: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val color = if (isIgnored) Color(0xFF757575) else when (result.status) {
        CheckStatus.GREEN -> Color(0xFF2E7D32)
        CheckStatus.YELLOW -> Color(0xFFF9A825)
        CheckStatus.RED -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (isIgnored) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(color, RoundedCornerShape(50)))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = result.title + if (isIgnored) " (Пропущено)" else "",
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(result.summary, style = MaterialTheme.typography.bodyMedium)

            if (result.details.isNotEmpty()) {
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                    Text(if (expanded) "Свернуть" else "Показать детали")
                }
                if (expanded) {
                    result.details.forEach { detail ->
                        val parts = detail.split("||")
                        val text = parts[0]
                        val pkgName = if (parts.size > 1) parts[1].split("|")[0] else ""
                        val reason = if (parts.size > 1 && parts[1].contains("|")) parts[1].substringAfter("|") else ""
                        
                        val displayText = if (reason.isNotEmpty()) "$text $reason" else text

                        if (pkgName.isNotEmpty()) {
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = Uri.parse("package:$pkgName")
                                    context.startActivity(intent)
                                }.padding(vertical = 4.dp)
                            )
                        } else {
                            Text(displayText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                    
                    // Кнопка "Пропустить" появляется ТОЛЬКО после разворота
                    if (result.fixLabel.contains("Пропустить") && !isIgnored) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onFix()        // Выполняем пропуск
                                expanded = false // Сворачиваем список
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            val formattedLabel = result.fixLabel.replace(" (", "\n(")
                            Text(text = formattedLabel, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Основная кнопка (Исправить) - только если это не "Пропустить" и не "Игнорировано"
            if (result.fixIntent != null && !isIgnored) {
                val showOutside = !result.fixLabel.contains("Пропустить") || result.details.isEmpty()
                val shouldShowButton = result.status != CheckStatus.GREEN || result.fixLabel.contains("Пропустить")
                
                if (showOutside && shouldShowButton) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onFix, colors = ButtonDefaults.buttonColors(containerColor = color)) {
                        val formattedLabel = result.fixLabel.replace(" (", "\n(")
                        Text(text = formattedLabel, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}