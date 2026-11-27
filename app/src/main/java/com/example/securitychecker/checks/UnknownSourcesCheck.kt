package com.example.securitychecker.checks

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object UnknownSourcesCheck {
    private const val OPSTR = "android:request_install_packages"
    private const val MIME_APK = "application/vnd.android.package-archive"

    fun run(context: Context): CheckResult {
        val title = "Установка из неизвестных источников"
        val pm = context.packageManager
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        
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

            // Проверяем наличие активности для APK и разрешения в манифесте
            val hasApkActivity = apkHandlers.any { it.activityInfo.packageName == pkgName }
            val hasRequestPermission = pkg.requestedPermissions?.contains("android.permission.REQUEST_INSTALL_PACKAGES") == true

            // Если приложение не умеет открывать APK и не просит разрешение - пропускаем
            if (!hasRequestPermission && !hasApkActivity) continue

            // Пытаемся узнать статус через AppOps
            var appOpsStatus = -1
            try {
                appOpsStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(OPSTR, appInfo.uid, pkgName)
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(OPSTR, appInfo.uid, pkgName)
                }
            } catch (_: Throwable) {}

            val appName = appInfo.loadLabel(pm).toString()
            val stores = setOf("ru.vk.store", "com.huawei.appmarket", "com.android.vending")
            
            // Если это магазин приложений - пропускаем (они безопасны по определению)
            if (pkgName in stores) continue

            // Логика добавления в список
            if (appOpsStatus == AppOpsManager.MODE_ALLOWED) {
                details.add("• $appName ($pkgName) — разрешено")
            } else if (appOpsStatus == AppOpsManager.MODE_DEFAULT || appOpsStatus == AppOpsManager.MODE_ERRORED) {
                // Если система скрывает статус, но приложение умеет ставить APK
                if (hasApkActivity || hasRequestPermission) {
                    details.add("• $appName ($pkgName) — может ставить APK (система скрывает)")
                }
            }
        }

        return if (details.isEmpty()) {
            CheckResult(
                "unknown", title, CheckStatus.GREEN,
                "Сторонних приложений с правом или возможностью установки APK не найдено.",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        } else {
            CheckResult(
                "unknown", title, CheckStatus.YELLOW,
                "Найдено ${details.size} приложений. Нажмите на приложение, чтобы перейти в настройки.",
                details.sorted(),
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES),
                fixLabel = "Пропустить (я доверяю этим приложениям)" // Кнопка появится после раскрытия
            )
        }
    }
}