package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
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
                // Получаем имя установщика (работает не на всех прошивках)
                val installer = pm.getInstallSourceInfo(pkg.packageName).installingPackageName
                
                if (installer == null || installer !in officialStores) {
                    val appName = appInfo.loadLabel(pm).toString()
                    val source = installer ?: "неизвестно"
                    details.add("• $appName ($source)")
                }
            } catch (_: Exception) {
                // Если система не отдает данные (Android 11+ Huawei), пропускаем
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