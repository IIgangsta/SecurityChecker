package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object UnknownSourcesCheck {
    fun run(context: Context): CheckResult {
        val title = "Установка из неизвестных источников"
        val pm = context.packageManager
        val suspicious = mutableListOf<String>()
        val details = mutableListOf<String>()

        // Получаем список всех установленных пакетов
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        for (pkg in packages) {
            val permissions = pkg.requestedPermissions ?: continue
            // Ищем тех, кто запрашивает разрешение на установку пакетов
            if (permissions.contains("android.permission.REQUEST_INSTALL_PACKAGES")) {
                val appInfo = pkg.applicationInfo ?: continue
                val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                
                // Пропускаем системные приложения (Google Play, системный установщик)
                if (isSystem) continue

                val name = appInfo.loadLabel(pm).toString()
                
                // RuStore и магазины считаем безопасными
                val stores = setOf("ru.vk.store", "com.huawei.appmarket", "com.android.vending")
                if (pkg.packageName !in stores) {
                    details.add("• $name (${pkg.packageName})")
                    suspicious.add(pkg.packageName)
                }
            }
        }

        return if (suspicious.isEmpty()) {
            CheckResult(
                "unknown", title, CheckStatus.GREEN, 
                "Сторонних приложений с правом установки APK не найдено.",
                fixIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        } else {
            CheckResult(
                "unknown", title, CheckStatus.YELLOW, 
                "Найдено ${suspicious.size} приложений, которые могут устанавливать APK.",
                details.sorted(),
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            )
        }
    }
}