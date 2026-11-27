package com.example.securitychecker.checks

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object UnknownSourcesCheck {
    private const val OPSTR = "android:request_install_packages"

    fun run(context: Context): CheckResult {
        val title = "Установка из неизвестных источников"
        val pm = context.packageManager
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val allowed = mutableListOf<String>()
        var hasHiddenData = false

        for (pkg in pm.getInstalledPackages(0)) {
            val ai = try { pm.getApplicationInfo(pkg.packageName, 0) } catch (_: Throwable) { null } ?: continue
            if ((ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            try {
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(OPSTR, ai.uid, pkg.packageName)
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(OPSTR, ai.uid, pkg.packageName)
                }

                when (mode) {
                    AppOpsManager.MODE_ALLOWED -> allowed += pkg.packageName
                    AppOpsManager.MODE_DEFAULT, AppOpsManager.MODE_ERRORED -> hasHiddenData = true
                }
            } catch (_: Throwable) {}
        }

        if (allowed.isEmpty() && !hasHiddenData) {
            return CheckResult(
                "unknown", title, CheckStatus.GREEN,
                "Ни одно приложение не имеет разрешения на установку APK.",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        }

        if (allowed.isEmpty() && hasHiddenData) {
            return CheckResult(
                "unknown", title, CheckStatus.GREEN,
                "Система скрывает данные о разрешениях на установку APK (защита приватности). Если вы не давали таких прав вручную — всё в порядке.",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        }

        val details = mutableListOf<String>()
        for (pkgName in allowed) {
            val ai = try { pm.getApplicationInfo(pkgName, 0) } catch (_: Throwable) { null } ?: continue
            val name = ai.loadLabel(pm).toString()
            details += "• $name ($pkgName)"
        }

        return CheckResult(
            "unknown", title, CheckStatus.YELLOW,
            "Найдено ${allowed.size} приложений с правом установки APK. Проверьте список.",
            details.sorted(),
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        )
    }
}