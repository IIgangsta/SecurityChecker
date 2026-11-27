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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
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
        // Верхняя панель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            // Кнопка закрытия
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterStart)
                    .background(Color.White, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { activity.finish() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Выход",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Название программы по центру
            Text(
                text = "SecurityChecker 1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }

        Column(Modifier.fillMaxSize().padding(top = 60.dp)) {
            if (!started) {
                // Экран приветствия
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()), // Добавили скролл на случай маленьких экранов
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(20.dp))
                    
                    Text("Модель: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodyLarge)
                    Text("Android: ${Build.VERSION.RELEASE}", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(Modifier.height(24.dp))

                    // Информационный блок
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Принципы работы",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Приложение не затрагивает системные файлы и не требует root-доступа. Оно не работает в фоне и не запрашивает опасных разрешений.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            Text(
                                "Что проверяем",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Анализ потенциальных уязвимостей для удаленного доступа (RAT), таких как RatHat, AnyDesk, TeamViewer, а также проверка прав администраторов, спецвозможностей и доступов к SMS/камере.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Рекомендация",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Регулярно проводите проверку для контроля безопасности вашего устройства. Все изменения в настройках вы вносите самостоятельно.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { started = true }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Начать проверку", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
            } else if (progressAnim.value < 0.99f) {
                // Экран загрузки с плавным кругом
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progressAnim.value },
                            modifier = Modifier.size(200.dp), 
                            strokeWidth = 12.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
                                    modifier = Modifier.size(120.dp), 
                                    strokeWidth = 10.dp, 
                                    color = ringColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Добавили знак %
                                    Text("$score%", style = MaterialTheme.typography.headlineLarge)
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
                                Button(
                                    onClick = { 
                                        started = false
                                        ignoredChecks = emptySet()
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) { Text("Повторить", style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }

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