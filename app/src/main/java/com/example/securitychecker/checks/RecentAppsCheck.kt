package com.example.securitychecker.checks

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object RecentAppsCheck {
    private const val PREFS_NAME = "SecurityCheckerPrefs"
    private const val KEY_LAST_CHECK = "last_check_time"

    fun run(context: Context): CheckResult {
        val title = "Новые приложения"
        val pm = context.packageManager
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Время последней проверки (по умолчанию 0, если проверок не было)
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK, 0L)
        val details = mutableListOf<String>()

        for (pkg in pm.getInstalledPackages(0)) {
            val appInfo = pkg.applicationInfo ?: continue
            if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            // Получаем время установки приложения
            val installTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    pm.getPackageInfo(pkg.packageName, 0).firstInstallTime
                } catch (_: Exception) { 0L }
            } else {
                @Suppress("DEPRECATION")
                pkg.firstInstallTime
            }

            // Если приложение было установлено ПОСЛЕ последней проверки, добавляем его
            if (lastCheckTime > 0 && installTime > lastCheckTime) {
                val appName = appInfo.loadLabel(pm).toString()
                details.add("• $appName (${pkg.packageName})")
            }
        }

        // Обновляем время последней проверки на текущее
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()

        return if (details.isEmpty()) {
            CheckResult(
                "recent_apps", title, CheckStatus.GREEN, 
                "С момента прошлой проверки новых приложений не установлено."
            )
        } else {
            CheckResult(
                "recent_apps", title, CheckStatus.YELLOW,
                "С момента прошлой проверки установлено ${details.size} новых приложений. Проверьте их!",
                details.sorted()
            )
        }
    }
}