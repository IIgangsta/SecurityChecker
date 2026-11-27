package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object UnknownSourcesCheck {
    private const val MIME_APK = "application/vnd.android.package-archive"

    fun run(context: Context): CheckResult {
        val title = "Установка из неизвестных источников"
        val pm = context.packageManager
        val details = mutableListOf<String>()

        // 1. Получаем список приложений, которые умеют открывать APK-файлы
        val apkIntent = Intent(Intent.ACTION_VIEW).apply {
            type = MIME_APK
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val apkHandlers = pm.queryIntentActivities(apkIntent, 0)

        // 2. Проверяем все приложения
        for (pkg in pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            val pkgName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue
            
            // Пропускаем системные приложения
            if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            // Ищем активность для APK и разрешение в манифесте
            val hasApkActivity = apkHandlers.any { it.activityInfo.packageName == pkgName }
            val hasRequestPermission = pkg.requestedPermissions?.contains("android.permission.REQUEST_INSTALL_PACKAGES") == true

            // Если есть и активность, и разрешение в манифесте — добавляем
            if (hasApkActivity && hasRequestPermission) {
                val appName = appInfo.loadLabel(pm).toString()
                
                // Пропускаем официальные магазины (чтобы не пугать пользователя)
                val stores = setOf("ru.vk.store", "com.huawei.appmarket", "com.android.vending")
                if (pkgName !in stores) {
                    // Формат: "• Название (Пакет)" с разделителем для клика
                    details.add("• $appName||$pkgName")
                }
            }
        }

        return if (details.isEmpty()) {
            CheckResult(
                "unknown", title, CheckStatus.GREEN,
                "Сторонних приложений с правом установки APK не найдено.",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        } else {
            CheckResult(
                "unknown", title, CheckStatus.YELLOW,
                "Найдено ${details.size} приложений, которые могут устанавливать APK. Проверьте список.",
                details.sorted(),
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES),
                fixLabel = "Пропустить (я доверяю этим приложениям)"
            )
        }
    }
}