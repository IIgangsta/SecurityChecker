package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object AndroidVersionCheck {
    fun run(context: Context): CheckResult {
        val title = "Версия Android"
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE

        val status = if (sdk < Build.VERSION_CODES.S) CheckStatus.YELLOW else CheckStatus.GREEN
        val summary = if (sdk < Build.VERSION_CODES.S) {
            "Android $release (API $sdk) — версия старше Android 12. Поддержка могла истечь."
        } else {
            "Android $release (API $sdk) — поддерживаемая версия."
        }

        // Кнопка "Пропустить" (если обновлений больше нет)
        val fixLabel = if (sdk < Build.VERSION_CODES.S) "Пропустить (нет обновлений)" else "Подробнее"
        val fixIntent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)

        return CheckResult("version", title, status, summary, fixIntent = fixIntent, fixLabel = fixLabel)
    }
}