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
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus
import com.example.securitychecker.ui.CheckCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
            onIgnore()
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
    var results by remember { mutableStateOf<List<CheckResult>>(emptyList()) }
    var currentStageText by remember { mutableStateOf("Инициализация...") }
    var ignoredChecks by remember { mutableStateOf(setOf<String>()) }

    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(started) {
        if (started) {
            progressAnim.snapTo(0f)
            
            val stages = listOf(
                "Патч...", "Спецвозможности...", "Администраторы...", 
                "Источники...", "Версия...", "Разрешения...",
                "Root-права...", "Блокировка...", "Отладка...",
                "VPN...", "Опасные приложения...", "Владелец...", "RAT..."
            )
            
            // ВЫНОСИМ ТЯЖЕЛЫЕ ПРОВЕРКИ В ФОНОВЫЙ ПОТОК
            val allResults = withContext(Dispatchers.IO) {
                CheckEngine.runAll(activity)
            }
            results = allResults

            val totalSteps = stages.size
            for (i in 0 until totalSteps) {
                currentStageText = stages[i]
                progressAnim.animateTo(
                    targetValue = (i + 1).toFloat() / totalSteps,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                )
                delay(50)
            }
        }
    }

    val score = remember(results, ignoredChecks) {
        if (results.isEmpty()) 0
        else {
            var totalPoints = 0
            for (r in results) {
                if (r.id in ignoredChecks) {
                    totalPoints += 100
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

    Box(Modifier.fillMaxSize()) {
        // Кнопка выхода (УМЕНЬШИЛИ ДИАМЕТР ДО 24, обводка 1dp, крестик 14dp)
        IconButton(
            onClick = { activity.finish() },
            modifier = Modifier
                .padding(12.dp)
                .size(24.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Выход",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }

        Column(Modifier.fillMaxSize().padding(top = 60.dp)) {
            if (!started) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("SecurityChecker", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Модель: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodyLarge)
                    Text("Android: ${Build.VERSION.RELEASE}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("Начать проверку")
                    }
                }
            } else if (progressAnim.value < 0.99f) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progressAnim.value },
                            modifier = Modifier.size(200.dp), 
                            strokeWidth = 14.dp
                        )
                        Text(
                            "${(progressAnim.value * 100).toInt()}%", 
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    Text(currentStageText, style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Column(Modifier.fillMaxSize()) {
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
                                modifier = Modifier.size(110.dp),
                                strokeWidth = 10.dp,
                                color = ringColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$score", style = MaterialTheme.typography.headlineLarge)
                                Text(
                                    text = when { score >= 80 -> "Защищено"; score >= 50 -> "Внимание"; else -> "Опасно" },
                                    style = MaterialTheme.typography.labelSmall, color = ringColor
                                )
                            }
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text("Средний балл", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Решите проблемы или пропустите неактуальные пункты.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { 
                                    started = false
                                    ignoredChecks = emptySet()
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
                                    activity.launchFix(r) { ignoredChecks = ignoredChecks + r.id }
                                }
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}