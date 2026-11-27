package com.example.securitychecker

import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.securitychecker.models.CheckResult
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
    var progress by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<CheckResult>>(emptyList()) }

    LaunchedEffect(started) {
        if (started && progress == 0) {
            val allResults = CheckEngine.runAll(activity)
            for (i in 1..10) {
                delay(150)
                progress = i * 10
            }
            results = allResults
        }
    }

    if (!started) {
        // Экран приветствия
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SecurityChecker 1.0.1", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Модель: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodyLarge)
            Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Начать проверку", style = MaterialTheme.typography.titleMedium)
            }
        }
    } else if (progress < 100) {
        // Экран загрузки
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Сканирование системы...", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress / 100f }, // Исправлено для новых версий Compose
                    modifier = Modifier.size(120.dp), 
                    strokeWidth = 8.dp
                )
                Text("$progress%", style = MaterialTheme.typography.headlineMedium)
            }
        }
    } else {
        // Экран результатов
        Column(Modifier.fillMaxSize()) {
            Text(
                "Отчет о проверке",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
            LazyColumn(Modifier.weight(1f)) {
                items(results) { r ->
                    CheckCard(r, onFix = { activity.launchFix(r) })
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
            Button(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                onClick = { 
                    started = false
                    progress = 0 
                    results = emptyList() 
                }
            ) { Text("Повторить проверку") }
        }
    }
}