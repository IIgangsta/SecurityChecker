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
        val remoteAccessPackages = listOf(
            "com.anydesk.anydeskandroid",
            "com.teamviewer.teamviewer.market.mobile",
            "com.teamviewer.quicksupport.market",
            "com.microsoft.rdc.androidx"
        )

        val details = mutableListOf<String>()
        for (pkg in remoteAccessPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                details.add("• ${pm.getApplicationLabel(appInfo)} ($pkg)")
            } catch (_: Exception) {}
        }

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
            CheckResult("rat", title, CheckStatus.GREEN, "Приложения удаленного доступа не обнаружены.")
        } else {
            CheckResult("rat", title, CheckStatus.RED, "Обнаружены признаки приложений удаленного доступа.", details, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}