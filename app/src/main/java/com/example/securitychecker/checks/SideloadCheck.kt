package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object SideloadCheck {
    fun run(context: Context): CheckResult {
        val title = "Скачано вручную (APK)"
        val pm = context.packageManager
        val details = mutableListOf<String>()

        val officialStores = setOf(
            "com.android.vending", // Google Play
            "ru.vk.store",         // RuStore
            "com.huawei.appmarket" // AppGallery
        )

        for (pkg in pm.getInstalledPackages(0)) {
            val appInfo = pkg.applicationInfo ?: continue
            if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            try {
                // Проверяем версию Android
                val installer: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ (API 30+) используем новый метод
                    pm.getInstallSourceInfo(pkg.packageName).installingPackageName
                } else {
                    // Android 10 и ниже используем старый метод
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(pkg.packageName)
                }
                
                if (installer == null || installer !in officialStores) {
                    val appName = appInfo.loadLabel(pm).toString()
                    val source = installer ?: "неизвестно"
                    details.add("• $appName ($source)")
                }
            } catch (_: Exception) {
                // Если система не отдает данные, пропускаем
            }
        }

        return if (details.isEmpty()) {
            CheckResult("sideload", title, CheckStatus.GREEN, "Все сторонние приложения установлены из официальных магазинов.")
        } else {
            CheckResult(
                "sideload", title, CheckStatus.YELLOW,
                "Найдены приложения, установленные вручную (APK). Это повышает риск заражения, так как они не проходили проверку в магазине.",
                details.sorted(),
                fixIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        }
    }
}