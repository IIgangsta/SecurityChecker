package com.example.securitychecker

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.ui.CheckCard

class MainActivity : ComponentActivity() {
    private var refreshKey by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val results = remember(refreshKey) { CheckEngine.runAll(this) }

                    Column(Modifier.fillMaxSize()) {
                        Text(
                            "SecurityChecker 1.0.0",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(Modifier.weight(1f)) {
                            items(results) { r ->
                                CheckCard(r, onFix = { launchFix(r) })
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }

                        Button(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            onClick = { refreshKey++ }
                        ) { Text("Пересчитать статусы") }
                    }
                }
            }
        }
    }

    private fun launchFix(result: CheckResult) {
        val intent = result.fixIntent ?: return
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Экран недоступен", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshKey++
    }
}