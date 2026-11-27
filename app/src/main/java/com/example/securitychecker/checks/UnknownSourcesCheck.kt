package com.example.securitychecker.checks

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
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

        for (pkg in pm.getInstalledPackages(0)) {
            if (appOps.checkOpNoThrow(OPSTR, Process.myUid(), pkg.packageName) == AppOpsManager.MODE_ALLOWED) {
                allowed += pkg.packageName
            }
        }

        if (allowed.isEmpty()) return CheckResult("unknown", title, CheckStatus.GREEN, "Никто не может ставить APK.", fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))

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
        val summary = if (suspicious.isEmpty()) "Только магазины приложений." else "Сторонние приложения могут ставить APK!"

        return CheckResult("unknown", title, status, summary, details, Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
    }
}