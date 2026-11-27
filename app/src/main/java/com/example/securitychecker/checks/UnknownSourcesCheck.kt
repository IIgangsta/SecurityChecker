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
            try {
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(OPSTR, pm.getApplicationInfo(pkg.packageName, 0).uid, pkg.packageName)
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(OPSTR, pm.getApplicationInfo(pkg.packageName, 0).uid, pkg.packageName)
                }

                when (mode) {
                    AppOpsManager.MODE_ALLOWED -> allowed += pkg.packageName
                    AppOpsManager.MODE_DEFAULT, AppOpsManager.MODE_ERRORED -> hasHiddenData = true
                }
            } catch (_: Throwable) {}
        }

        if (allowed.isEmpty() && !hasHiddenData) {
            return CheckResult("unknown", title, CheckStatus.GREEN, "Ни одно приложение не имеет разрешения на установку APK.", fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
        }

        if (allowed.isEmpty() && hasHiddenData) {
            return CheckResult("unknown", title, CheckStatus.GREEN, "Система скрывает данные о разрешениях на установку APK (защита приватности).", fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
        }

        val stores = setOf("com.android.vending", "ru.vk.store", "com.huawei.appmarket")
        val suspicious = mutableListOf<String>()
        val details = mutableListOf<String>()

        for (pkgName in allowed) {
            val ai = try { pm.getApplicationInfo(pkgName, 0) } catch (_: Throwable) { null }
            if (ai == null) continue
            val isSystem = (ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val name = ai.loadLabel(pm).toString()
            details += "• $name ($pkgName)"
            if (!isSystem && pkgName !in stores) suspicious += pkgName
        }

        val status = if (suspicious.isEmpty()) CheckStatus.GREEN else CheckStatus.RED
        val summary = if (suspicious.isEmpty()) "Право установки APK есть только у магазинов приложений." else "Сторонние приложения могут ставить APK! Проверьте список."

        return CheckResult("unknown", title, status, summary, details, Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
    }
}