package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object OverlayCheck {
    fun run(context: Context): CheckResult {
        val title = "Наложение окон (Оверлеи)"
        val pm = context.packageManager
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val details = mutableListOf<String>()

        val opstr = "android:system_alert_window"

        for (pkg in pm.getInstalledPackages(0)) {
            val appInfo = pkg.applicationInfo ?: continue
            if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            try {
                // Проверяем, разрешено ли приложению рисовать поверх других
                val mode = appOps.unsafeCheckOpNoThrow(opstr, appInfo.uid, pkg.packageName)
                if (mode == android.app.AppOpsManager.MODE_ALLOWED) {
                    val appName = appInfo.loadLabel(pm).toString()
                    details.add("• $appName (${pkg.packageName})")
                }
            } catch (_: Throwable) {}
        }

        return if (details.isEmpty()) {
            CheckResult("overlay", title, CheckStatus.GREEN, "Приложений, рисующих поверх других, не обнаружено.")
        } else {
            CheckResult(
                "overlay", title, CheckStatus.YELLOW,
                "Обнаружены приложения, которые могут рисовать поверх других. Внимание: они могут использовать это для кражи паролей (фейковые окна)!",
                details.sorted(),
                fixIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            )
        }
    }
}