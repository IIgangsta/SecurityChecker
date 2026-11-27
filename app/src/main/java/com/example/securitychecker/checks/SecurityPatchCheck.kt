package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object SecurityPatchCheck {
    fun run(context: Context): CheckResult {
        val patch = Build.VERSION.SECURITY_PATCH
        val title = "Патч безопасности"

        if (patch.isNullOrBlank() || patch == "unknown") {
            return CheckResult("patch", title, CheckStatus.RED, "Не удалось определить дату патча.", fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS))
        }

        val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(patch) } catch (e: Exception) { null }
        if (date == null) return CheckResult("patch", title, CheckStatus.YELLOW, "Формат даты: $patch", fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS))

        val months = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - date.time) / 30.0
        
        val summary = when {
            months < 6.0  -> "Патч от $patch — свежий."
            months < 12.0 -> "Патчу от $patch уже %.1f мес. Обновите систему, если доступно.".format(months)
            else          -> "Система сообщает дату: $patch. Если в настройках телефона (О телефоне) дата новее — значит, производитель не обновил системный API, но телефон защищен."
        }

        val status = when {
            months < 6.0  -> CheckStatus.GREEN
            months < 12.0 -> CheckStatus.YELLOW
            else          -> CheckStatus.RED
        }

        // Кнопка "Пропустить" (для старых телефонов без обновлений)
        val fixLabel = if (months >= 12.0) "Пропустить (нет обновлений)" else "Исправить"
        val fixIntent = if (months >= 12.0) Intent(Settings.ACTION_DEVICE_INFO_SETTINGS) else Intent(Settings.ACTION_SECURITY_SETTINGS)

        return CheckResult("patch", title, status, summary, fixIntent = fixIntent, fixLabel = fixLabel)
    }
}