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

        val apkIntent = Intent(Intent.ACTION_VIEW).apply {
            type = MIME_APK
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val apkHandlers = pm.queryIntentActivities(apkIntent, 0)

        for (pkg in pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            val pkgName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue
            
            if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            val hasApkActivity = apkHandlers.any { it.activityInfo.packageName == pkgName }
            val hasRequestPermission = pkg.requestedPermissions?.contains("android.permission.REQUEST_INSTALL_PACKAGES") == true

            if (hasApkActivity || hasRequestPermission) {
                val appName = appInfo.loadLabel(pm).toString()
                val stores = setOf("ru.vk.store", "com.huawei.appmarket", "com.android.vending")
                
                if (pkgName !in stores) {
                    val reason = when {
                        hasApkActivity && hasRequestPermission -> "умеет открывать APK и просит права"
                        hasApkActivity -> "умеет открывать APK"
                        else -> "просит права на установку"
                    }
                    details.add("• $appName||$pkgName|($reason)")
                }
            }
        }

        return if (details.isEmpty()) {
            CheckResult(
                "unknown", title, CheckStatus.GREEN,
                "Система Android скрывает точный список. Проверьте вручную: Настройки → Приложения → Специальный доступ. Никогда не устанавливайте APK из SMS или подозрительных сайтов!",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        } else {
            CheckResult(
                "unknown", title, CheckStatus.YELLOW,
                "Найдено ${details.size} потенциальных установщиков. Никогда не устанавливайте APK из SMS или подозрительных сайтов!",
                details.sorted(),
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES),
                fixLabel = "Пропустить (я доверяю этим приложениям)"
            )
        }
    }
}