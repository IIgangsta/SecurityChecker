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

        // 1. Ищем приложения, которые умеют открывать APK
        val apkIntent = Intent(Intent.ACTION_VIEW).apply {
            type = MIME_APK
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val apkHandlers = pm.queryIntentActivities(apkIntent, 0)

        // 2. Проверяем все несистемные приложения
        for (pkg in pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            val pkgName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue
            
            if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            val hasApkActivity = apkHandlers.any { it.activityInfo.packageName == pkgName }
            val hasRequestPermission = pkg.requestedPermissions?.contains("android.permission.REQUEST_INSTALL_PACKAGES") == true

            // 3. Если приложение умеет открывать APK ИЛИ просит разрешение
            if (hasApkActivity || hasRequestPermission) {
                val appName = appInfo.loadLabel(pm).toString()
                val stores = setOf("ru.vk.store", "com.huawei.appmarket", "com.android.vending")
                
                if (pkgName !in stores) {
                    val reason = when {
                        hasApkActivity && hasRequestPermission -> "умеет открывать APK и просит права"
                        hasApkActivity -> "умеет открывать APK"
                        else -> "просит права на установку"
                    }
                    // Формат для клика: "• Имя||пакет|(причина)"
                    details.add("• $appName||$pkgName|($reason)")
                }
            }
        }

        return if (details.isEmpty()) {
            CheckResult(
                "unknown", title, CheckStatus.GREEN,
                "Система Android скрывает точный список приложений с правом установки. Проверьте вручную: Настройки → Приложения → Специальный доступ → Установка неизвестных приложений.",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        } else {
            CheckResult(
                "unknown", title, CheckStatus.YELLOW,
                "Найдено ${details.size} потенциальных установщиков. Система может скрывать часть данных, проверьте вручную.",
                details.sorted(),
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES),
                fixLabel = "Пропустить (я доверяю этим приложениям)"
            )
        }
    }
}