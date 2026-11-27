package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object DangerousAppsCheck {
    fun run(context: Context): CheckResult {
        val title = "Опасные приложения"
        val pm = context.packageManager
        val details = mutableListOf<String>()

        val dangerousPackages = mapOf(
            "com.topjohnwu.magisk" to "Magisk",
            "eu.chainfire.supersu" to "SuperSU",
            "de.robv.android.xposed.installer" to "Xposed"
        )

        for ((pkg, name) in dangerousPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                details.add("• $name ($pkg)")
            } catch (_: Exception) {}
        }

        return if (details.isEmpty()) {
            CheckResult("dangerous_apps", title, CheckStatus.GREEN, "Опасных приложений не найдено.")
        } else {
            CheckResult("dangerous_apps", title, CheckStatus.RED, "Найдены приложения для модификации системы.", details, Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }
    }
}