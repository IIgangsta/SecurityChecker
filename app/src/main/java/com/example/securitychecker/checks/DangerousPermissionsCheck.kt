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

        // Список разрешений, которые мы ищем
        val targetPermissions = listOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW
        )

        for (perm in targetPermissions) {
            try {
                // Ищем ВСЕ приложения, которым выдано это разрешение (самый надежный метод)
                val packages = pm.getPackagesHoldingPermissions(arrayOf(perm), 0)
                
                for (pkgInfo in packages) {
                    // Пропускаем саму себя
                    if (pkgInfo.packageName == context.packageName) continue
                    
                    val appInfo = pkgInfo.applicationInfo ?: continue
                    
                    // Пропускаем системные приложения (чтобы не пугать пользователя)
                    if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

                    val appName = appInfo.loadLabel(pm).toString()
                    
                    // Переводим техническое название разрешения на русский
                    val permName = when (perm) {
                        android.Manifest.permission.RECEIVE_SMS -> "Прием SMS"
                        android.Manifest.permission.READ_SMS -> "Чтение SMS"
                        android.Manifest.permission.SEND_SMS -> "Отправка SMS"
                        android.Manifest.permission.CAMERA -> "Камера"
                        android.Manifest.permission.SYSTEM_ALERT_WINDOW -> "Поверх окон"
                        else -> "Другое"
                    }

                    details.add("• $appName ($permName)")
                }
            } catch (_: Throwable) {
                // Игнорируем ошибки, если система заблокирует доступ к конкретному пакету
            }
        }

        // Убираем дубликаты (если приложение имеет и чтение, и отправку SMS)
        val uniqueDetails = details.distinct()

        return if (uniqueDetails.isEmpty()) {
            CheckResult(
                "danger", title, CheckStatus.GREEN,
                "У сторонних приложений нет доступа к SMS, камере или экрану.",
                fixIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        } else {
            CheckResult(
                "danger", title, CheckStatus.YELLOW,
                "Найдено ${uniqueDetails.size} разрешений у сторонних приложений. Проверьте список.",
                uniqueDetails,
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        }
    }
}