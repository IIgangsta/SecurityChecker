package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object RatCheck {
    fun run(context: Context): CheckResult {
        val title = "Удаленный доступ (RAT)"
        val pm = context.packageManager
        
        // Список известных легальных программ удаленного доступа (AnyDesk, TeamViewer и т.д.)
        // Если они есть на телефоне, это не обязательно вирус, но требует внимания.
        val remoteAccessPackages = listOf(
            "com.anydesk.anydeskandroid",
            "com.teamviewer.teamviewer.market.mobile",
            "com.teamviewer.quicksupport.market",
            "com.microsoft.rdc.androidx",
            "com.parsecgaming.parsec",
            "com.remotepc.rpcc",
            "com.logmein.rescue"
        )

        val details = mutableListOf<String>()
        
        // 1. Проверяем наличие известных легальных RAT-приложений
        for (pkg in remoteAccessPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val name = pm.getApplicationLabel(appInfo).toString()
                details.add("• $name ($pkg)")
            } catch (_: Exception) {}
        }

        // 2. Проверяем спецвозможности (так как RAT часто их использует)
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        val thirdPartyServices = enabledServices.filter {
            val app = it.resolveInfo?.serviceInfo?.applicationInfo
            app != null && (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
        }

        if (thirdPartyServices.isNotEmpty()) {
            details.add("• Активны сторонние службы спецвозможностей: ${thirdPartyServices.size}")
        }

        return if (details.isEmpty()) {
            CheckResult("rat", title, CheckStatus.GREEN, "Приложения удаленного доступа и активные сторонние спецвозможности не обнаружены.")
        } else {
            CheckResult(
                "rat", title, CheckStatus.RED,
                "Обнаружены признаки приложений удаленного доступа или активных спецвозможностей. Проверьте список.",
                details,
                fixIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            )
        }
    }
}