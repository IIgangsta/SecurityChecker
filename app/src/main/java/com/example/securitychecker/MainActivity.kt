package com.example.securitychecker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus
import com.example.securitychecker.ui.CheckCard
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    SecurityCheckerScreen(this)
                }
            }
        }
    }

    fun launchFix(result: CheckResult, onIgnore: () -> Unit) {
        if (result.fixLabel.contains("Пропустить")) {
            onIgnore() // Пользователь нажал "Пропустить"
        } else {
            val intent = result.fixIntent ?: return
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Экран недоступен", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SecurityCheckerScreen(activity: MainActivity) {
    var started by remember { mutableStateOf(false) }
    var targetProgress by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<CheckResult>>(emptyList()) }
    var currentStageText by remember { mutableStateOf("Инициализация...") }
    
    // Список игнорируемых проверок (по ID)
    var ignoredChecks by remember { mutableStateOf(setOf<String>()) }

    val animatedProgress by animateFloatAsState(targetValue = targetProgress / 100f, animationSpec = tween(300), label = "Progress")

    LaunchedEffect(started) {
        if (started && targetProgress == 0) {
            val stages = listOf("Проверка патча...", "Спецвозможности...", "Администраторы...", "Источники...", "Версия Android...", "Разрешения...")
            val allResults = CheckEngine.runAll(activity)
            for (i in stages.indices) {
                currentStageText = stages[i]
                delay(350)
                targetProgress = ((i + 1) * 100) / stages.size
            }
            results = allResults
        }
    }

    // Подсчет оценки с учетом пропущенных
    val score = remember(results, ignoredChecks) {
        if (results.isEmpty()) 0
        else {
            var totalPoints = 0
            for (r in results) {
                if (r.id in ignoredChecks) {
                    totalPoints += 100 // Если пропущено, считаем как 100 (зеленый)
                } else {
                    totalPoints += when (r.status) {
                        CheckStatus.GREEN -> 100
                        CheckStatus.YELLOW -> 60
                        CheckStatus.RED -> 20
                    }
                }
            }
            totalPoints / results.size
        }
    }

    if (!started) {
        // Экран приветствия
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("SecurityChecker", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text("Модель: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodyLarge)
                Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Начать проверку", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    } else if (targetProgress < 100) {
        // Экран загрузки
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(200.dp), strokeWidth = 14.dp)
                Text("${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.displayLarge)
            }
            Spacer(Modifier.height(32.dp))
            Text(currentStageText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // Экран результатов
        Box(Modifier.fillMaxSize()) {
            // Кнопка выхода (крестик) в левом верхнем углу
            IconButton(
                onClick = { activity.finish() }, // Закрывает приложение
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Выход")
            }

            Column(Modifier.fillMaxSize().padding(top = 60.dp)) {
                // Компактный блок с оценкой (слева круг, справа текст)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ringColor = when {
                        score >= 80 -> Color(0xFF2E7D32)
                        score >= 50 -> Color(0xFFF9A825)
                        else -> Color(0xFFC62828)
                    }

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(110.dp), // Уменьшили круг
                            strokeWidth = 10.dp,
                            color = ringColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$score", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                text = when { score >= 80 -> "Защищено"; score >= 50 -> "Внимание"; else -> "Опасно" },
                                style = MaterialTheme.typography.labelSmall,
                                color = ringColor
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(20.dp))
                    
                    Column {
                        Text("Средний балл безопасности", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Решите проблемы в списке ниже или пропустите неактуальные пункты, чтобы повысить оценку.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                started = false; targetProgress = 0; results = emptyList(); ignoredChecks = emptySet()
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { Text("Повторить", style = MaterialTheme.typography.labelMedium) }
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f)) {
                    items(results) { r ->
                        CheckCard(
                            result = r,
                            isIgnored = r.id in ignoredChecks,
                            onFix = { 
                                activity.launchFix(r) {
                                    ignoredChecks = ignoredChecks + r.id // Добавляем в игнорируемые
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}