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
        val (status, summary) = when {
            months < 6.0  -> CheckStatus.GREEN to "Патч от $patch — свежий."
            months < 12.0 -> CheckStatus.YELLOW to "Патчу от $patch уже %.1f мес.".format(months)
            else          -> CheckStatus.RED to "Патчу от $patch больше года."
        }
        return CheckResult("patch", title, status, summary, fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
}