package com.example.securitychecker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

@Composable
fun CheckCard(result: CheckResult, onFix: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val color = when (result.status) {
        CheckStatus.GREEN -> Color(0xFF2E7D32)
        CheckStatus.YELLOW -> Color(0xFFF9A825)
        CheckStatus.RED -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(color, RoundedCornerShape(50)))
                Spacer(Modifier.width(10.dp))
                Text(result.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(result.summary, style = MaterialTheme.typography.bodyMedium)

            if (result.details.isNotEmpty()) {
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                    Text(if (expanded) "Свернуть" else "Показать детали")
                }
                if (expanded) {
                    result.details.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }

            if (result.fixIntent != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onFix, colors = ButtonDefaults.buttonColors(containerColor = color)) {
                    Text(result.fixLabel)
                }
            }
        }
    }
}