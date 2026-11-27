package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object SuspiciousNamesCheck {
    fun run(context: Context): CheckResult {
        val title = "Маскировка под систему"
        val pm = context.packageManager
        val details = mutableListOf<String>()

        // Список паттернов, которые часто используют вирусы, чтобы притвориться системными
        val suspiciousPatterns = listOf(
            "com.google.android.protect",
            "com.android.system.update",
            "com.android.system.service",
            "com.google.android.system",
            "com.android.security.patch"
        )

        for (pkgName in suspiciousPatterns) {
            try {
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                // Если нашли, но флаг FLAG_SYSTEM НЕ установлен — это маскировка!
                if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                    val appName = appInfo.loadLabel(pm).toString()
                    details.add("• $appName ($pkgName) — маскируется под систему!")
                }
            } catch (_: Exception) {
                // Пакет не найден — отлично, значит нет подделки
            }
        }

        return if (details.isEmpty()) {
            CheckResult("suspicious_names", title, CheckStatus.GREEN, "Подозрительных системных приложений не обнаружено.")
        } else {
            CheckResult(
                "suspicious_names", title, CheckStatus.RED, 
                "Обнаружены приложения, маскирующиеся под системные!", 
                details, 
                fixIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        }
    }
}