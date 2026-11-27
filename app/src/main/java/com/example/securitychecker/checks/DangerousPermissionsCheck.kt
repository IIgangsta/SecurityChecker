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
        
        // Используем Map для группировки: Ключ = Имя приложения, Значение = Пакет и список разрешений
        data class AppPerms(val pkgName: String, val appName: String, val permissions: MutableList<String> = mutableListOf())
        val appsMap = mutableMapOf<String, AppPerms>()

        val targetPermissions = listOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
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
                        android.Manifest.permission.RECEIVE_SMS -> "SMS (прием)"
                        android.Manifest.permission.READ_SMS -> "SMS (чтение)"
                        android.Manifest.permission.SEND_SMS -> "SMS (отправка)"
                        android.Manifest.permission.CAMERA -> "Камера"
                        android.Manifest.permission.SYSTEM_ALERT_WINDOW -> "Поверх окон"
                        android.Manifest.permission.ACCESS_FINE_LOCATION -> "Гео (точная)"
                        android.Manifest.permission.ACCESS_COARSE_LOCATION -> "Гео (примерная)"
                        else -> "Другое"
                    }

                    // Группируем по имени пакета
                    val appData = appsMap.getOrPut(pkgInfo.packageName) { AppPerms(pkgInfo.packageName, appName) }
                    if (!appData.permissions.contains(permName)) {
                        appData.permissions.add(permName)
                    }
                }
            } catch (_: Throwable) {}
        }

        // Формируем итоговый список строк
        val details = mutableListOf<String>()
        for ((_, appData) in appsMap) {
            // Сортируем разрешения по алфавиту для красоты
            val permsString = appData.permissions.sorted().joinToString(", ")
            // Формат: "• Имя (Разрешение 1, Разрешение 2)||пакет"
            details.add("• ${appData.appName} ($permsString)||${appData.pkgName}")
        }

        return if (details.isEmpty()) {
            CheckResult(
                "danger", title, CheckStatus.GREEN,
                "У сторонних приложений нет доступа к SMS, камере или геолокации.",
                fixIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        } else {
            CheckResult(
                "danger", title, CheckStatus.YELLOW,
                "Найдено ${details.size} приложений с опасными правами. Нажмите на приложение для настройки.",
                details.sorted(),
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        }
    }
}