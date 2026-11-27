package com.example.securitychecker

import android.content.Context
import com.example.securitychecker.checks.*
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object CheckEngine {
    fun runAll(context: Context): List<CheckResult> {
        val results = mutableListOf<CheckResult>()

        fun safeRun(id: String, title: String, block: () -> CheckResult) {
            try {
                results.add(block())
            } catch (e: Throwable) {
                results.add(
                    CheckResult(
                        id = id,
                        title = title,
                        status = CheckStatus.RED,
                        summary = "Ошибка выполнения: ${e.localizedMessage ?: "неизвестная"}"
                    )
                )
            }
        }

        safeRun("patch", "Патч безопасности") { SecurityPatchCheck.run(context) }
        safeRun("accessibility", "Спецвозможности") { AccessibilityCheck.run(context) }
        safeRun("admin", "Администраторы") { DeviceAdminCheck.run(context) }
        safeRun("unknown", "Неизвестные источники") { UnknownSourcesCheck.run(context) }
        safeRun("version", "Версия Android") { AndroidVersionCheck.run(context) }
        safeRun("danger", "Опасные разрешения") { DangerousPermissionsCheck.run(context) }
        safeRun("root", "Root-права") { RootCheck.run(context) }
        safeRun("screenlock", "Блокировка экрана") { ScreenLockCheck.run(context) }
        safeRun("devopts", "Режим разработчика") { DeveloperOptionsCheck.run(context) }
        safeRun("vpn", "VPN / Прокси") { VpnCheck.run(context) }
        safeRun("dangerous_apps", "Опасные приложения") { DangerousAppsCheck.run(context) }
        safeRun("device_owner", "Владелец устройства") { DeviceOwnerCheck.run(context) }
        safeRun("rat", "Удаленный доступ (RAT)") { RatCheck.run(context) }
        safeRun("suspicious_names", "Маскировка под систему") { SuspiciousNamesCheck.run(context) }
        // Новые проверки
        safeRun("overlay", "Наложение окон (Оверлеи)") { OverlayCheck.run(context) }
        safeRun("sideload", "Скачано вручную (APK)") { SideloadCheck.run(context) }
        safeRun("recent_apps", "Новые приложения") { RecentAppsCheck.run(context) }
        return results
    }
}