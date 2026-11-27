package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object AccessibilityCheck {
    fun run(context: Context): CheckResult {
        val title = "Специальные возможности"
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        if (enabled.isEmpty()) return CheckResult("accessibility", title, CheckStatus.GREEN, "Службы не активны.", fixIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

        val thirdParty = enabled.filter { 
            val app = it.resolveInfo?.serviceInfo?.applicationInfo
            app != null && (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 
        }

        val details = enabled.map {
            val app = it.resolveInfo?.serviceInfo?.applicationInfo
            "• ${app?.loadLabel(context.packageManager)} (${app?.packageName})"
        }

        return if (thirdParty.isEmpty()) {
            CheckResult("accessibility", title, CheckStatus.GREEN, "Только системные службы.", details, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } else {
            CheckResult("accessibility", title, CheckStatus.RED, "Сторонняя служба может нажимать кнопки!", details, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}