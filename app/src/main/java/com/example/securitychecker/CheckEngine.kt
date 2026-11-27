package com.example.securitychecker

import android.content.Context
import com.example.securitychecker.checks.*
import com.example.securitychecker.models.CheckResult

object CheckEngine {
    fun runAll(context: Context): List<CheckResult> = listOf(
        SecurityPatchCheck.run(context),
        AccessibilityCheck.run(context),
        DeviceAdminCheck.run(context),
        UnknownSourcesCheck.run(context),
        AndroidVersionCheck.run(context),
        DangerousPermissionsCheck.run(context),
        RootCheck.run(context),
        ScreenLockCheck.run(context),
        DeveloperOptionsCheck.run(context),
        VpnCheck.run(context),
        // Новые проверки
        DangerousAppsCheck.run(context),
        DeviceOwnerCheck.run(context),
        RatCheck.run(context)
    )
}