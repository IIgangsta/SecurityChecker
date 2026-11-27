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
        val details = mutableListOf<String>()

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
                        android.Manifest.permission.RECEIVE_SMS -> "Прием SMS"
                        android.Manifest.permission.READ_SMS -> "Чтение SMS"
                        android.Manifest.permission.SEND_SMS -> "Отправка SMS"
                        android.Manifest.permission.CAMERA -> "Камера"
                        android.Manifest.permission.SYSTEM_ALERT_WINDOW -> "Поверх окон"
                        android.Manifest.permission.ACCESS_FINE_LOCATION -> "Геолокация (точная)"
                        android.Manifest.permission.ACCESS_COARSE_LOCATION -> "Геолокация (примерная)"
                        else -> "Другое"
                    }

                    // Формат: "Имя приложения|Пакет|Разрешение"
                    details.add("• $appName ($permName)||${pkgInfo.packageName}")
                }
            } catch (_: Throwable) {}
        }

        // Убираем дубликаты по имени пакета и разрешению
        val uniqueDetails = details.distinctBy { it.split("||")[0] + it.split("||")[1] }

        return if (uniqueDetails.isEmpty()) {
            CheckResult(
                "danger", title, CheckStatus.GREEN,
                "У сторонних приложений нет доступа к SMS, камере или геолокации.",
                fixIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        } else {
            CheckResult(
                "danger", title, CheckStatus.YELLOW,
                "Найдено ${uniqueDetails.size} разрешений. Нажмите на приложение, чтобы открыть его настройки.",
                uniqueDetails,
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        }
    }
}