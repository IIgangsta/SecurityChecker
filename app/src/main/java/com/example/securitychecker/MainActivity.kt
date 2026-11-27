package com.example.securitychecker

import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    fun launchFix(result: CheckResult) {
        val intent = result.fixIntent ?: return
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Экран недоступен", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun SecurityCheckerScreen(activity: MainActivity) {
    var started by remember { mutableStateOf(false) }
    var targetProgress by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<CheckResult>>(emptyList()) }
    var currentStageText by remember { mutableStateOf("Инициализация...") }

    // Плавная анимация прогресса
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress / 100f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(started) {
        if (started && targetProgress == 0) {
            // Имитируем пошаговую проверку
            val stages = listOf(
                "Проверка патча безопасности...",
                "Проверка спецвозможностей...",
                "Поиск администраторов...",
                "Анализ неизвестных источников...",
                "Проверка версии Android...",
                "Поиск опасных разрешений..."
            )

            val allResults = CheckEngine.runAll(activity)

            for (i in stages.indices) {
                currentStageText = stages[i]
                delay(400) // Задержка для визуального эффекта
                targetProgress = ((i + 1) * 100) / stages.size
            }
            results = allResults
        }
    }

    // Подсчет общей оценки
    val score = remember(results) {
        if (results.isEmpty()) 0
        else {
            val green = results.count { it.status == CheckStatus.GREEN }
            val yellow = results.count { it.status == CheckStatus.YELLOW }
            val red = results.count { it.status == CheckStatus.RED }
            (green * 100 + yellow * 60 + red * 20) / results.size
        }
    }

    if (!started) {
        // Экран приветствия
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SecurityChecker 1.0.7", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Модель: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodyLarge)
            Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Начать проверку", style = MaterialTheme.typography.titleMedium)
            }
        }
    } else if (targetProgress < 100) {
        // Экран загрузки
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 16.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.displayLarge)
            }
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                text = currentStageText, 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        // Экран результатов
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val ringColor = when {
                        score >= 80 -> Color(0xFF2E7D32) // Зеленый
                        score >= 50 -> Color(0xFFF9A825) // Оранжевый
                        else -> Color(0xFFC62828)        // Красный
                    }

                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(180.dp),
                        strokeWidth = 14.dp,
                        color = ringColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$score", style = MaterialTheme.typography.displayLarge)
                        Text(
                            text = when {
                                score >= 80 -> "Защищено"
                                score >= 50 -> "Внимание"
                                else -> "Опасно"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = ringColor
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "Средний балл безопасности. Решите проблемы в списке ниже, чтобы повысить оценку.",
                    modifier = Modifier.padding(horizontal = 32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        started = false
                        targetProgress = 0
                        results = emptyList() 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text("Повторить проверку", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(Modifier.weight(1f)) {
                items(results) { r ->
                    CheckCard(r, onFix = { activity.launchFix(r) })
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}