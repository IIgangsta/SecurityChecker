package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus
import java.io.File

object RootCheck {
    fun run(context: Context): CheckResult {
        val title = "Root-права"
        val details = mutableListOf<String>()
        var isRooted = false

        val paths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/data/local/bin/su", "/data/local/xbin/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                isRooted = true
                details.add("• Найден бинарник: $path")
            }
        }

        val rootPackages = listOf("com.topjohnwu.magisk", "eu.chainfire.supersu")
        val pm = context.packageManager
        for (pkg in rootPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                isRooted = true
                details.add("• Найдено приложение: $pkg")
            } catch (_: Exception) {}
        }

        return if (!isRooted) {
            CheckResult("root", title, CheckStatus.GREEN, "Root-права не обнаружены.")
        } else {
            CheckResult("root", title, CheckStatus.RED, "Обнаружены Root-права.", details, Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }
}