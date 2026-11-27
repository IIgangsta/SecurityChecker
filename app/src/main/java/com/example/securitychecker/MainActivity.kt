package com.example.securitychecker

import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
            
            val allResults = CheckEngine.runAll(activity)
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
        // Кнопка закрытия (уменьшили до 24dp, подняли выше, чтобы не было пустой полосы)
        Box(
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp) // Уменьшили отступ сверху
                .size(24.dp) // Еще меньше
                .background(Color.White, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { activity.finish() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Выход",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Контент подняли наверх (отступ 45dp вместо 70dp)
        Column(Modifier.fillMaxSize().padding(top = 45.dp)) {
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
                            strokeWidth = 16.dp, // Сделали толще, чтобы выглядело объемнее
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
                // Экран результатов
                Column(Modifier.fillMaxSize()) {
                    // Блок с оценкой (со скругленными углами и тенью)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp), // Отступы вокруг карточки
                        shape = RoundedCornerShape(16.dp), // Скругленные углы
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Тень
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                    modifier = Modifier.size(100.dp), // Чуть уменьшили круг
                                    strokeWidth = 12.dp,
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
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Средний балл", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (score == 100) "Все проверки пройдены отлично!" else "Решите проблемы или пропустите неактуальные пункты.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                // Объемная кнопка "Повторить" с тенью
                                Button(
                                    onClick = { 
                                        started = false
                                        ignoredChecks = emptySet()
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp) // Тень для кнопки
                                ) { Text("Повторить", style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }

                    // Небольшой отступ перед списком
                    Spacer(Modifier.height(8.dp))

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