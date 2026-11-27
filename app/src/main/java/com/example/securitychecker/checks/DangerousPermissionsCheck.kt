package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object DangerousPermissionsCheck {
    fun run(context: Context): CheckResult {
        val title = "Опасные разрешения"
        val pm = context.packageManager
        data class AppPerms(val pkgName: String, val appName: String, val permissions: MutableList<String> = mutableListOf())
        val appsMap = mutableMapOf<String, AppPerms>()

        val targetPermissions = listOf(
            android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS, android.Manifest.permission.CAMERA,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW, android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CALL_PHONE
        )

        for (perm in targetPermissions) {
            try {
                val packages = pm.getPackagesHoldingPermissions(arrayOf(perm), 0)
                for (pkgInfo in packages) {
                    if (pkgInfo.packageName == context.packageName) continue
                    val appInfo = pkgInfo.applicationInfo ?: continue
                    if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue
                    val appName = appInfo.loadLabel(pm).toString()
                    val permName = when (perm) {
                        android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS -> "SMS"
                        android.Manifest.permission.SEND_SMS -> "Отправка SMS"
                        android.Manifest.permission.CAMERA -> "Камера"
                        android.Manifest.permission.SYSTEM_ALERT_WINDOW -> "Поверх окон"
                        android.Manifest.permission.RECORD_AUDIO -> "Микрофон"
                        android.Manifest.permission.CALL_PHONE -> "Звонки"
                        else -> "Другое"
                    }
                    val appData = appsMap.getOrPut(pkgInfo.packageName) { AppPerms(pkgInfo.packageName, appName) }
                    if (!appData.permissions.contains(permName)) appData.permissions.add(permName)
                }
            } catch (_: Throwable) {}
        }

        val details = mutableListOf<String>()
        for ((_, appData) in appsMap) {
            details.add("• ${appData.appName} (${appData.permissions.sorted().joinToString(", ")})||${appData.pkgName}")
        }

        return if (details.isEmpty()) {
            CheckResult("danger", title, CheckStatus.GREEN, "Опасных разрешений нет.")
        } else {
            CheckResult(
                "danger", title, CheckStatus.YELLOW, 
                "Найдено ${details.size} приложений с опасными правами.", 
                details.sorted(), 
                fixIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS),
                fixLabel = "Пропустить (я доверяю этим приложениям)" // <-- Добавили кнопку
            )
        }
    }
}