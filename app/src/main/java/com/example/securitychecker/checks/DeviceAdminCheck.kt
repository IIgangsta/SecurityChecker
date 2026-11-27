package com.example.securitychecker.checks

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object DeviceAdminCheck {
    fun run(context: Context): CheckResult {
        val title = "Администраторы устройства"
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admins = dpm.activeAdmins ?: emptyList()

        if (admins.isEmpty()) {
            return CheckResult(
                "admin", title, CheckStatus.GREEN, 
                "Активных администраторов не найдено. Если вы включили «Облако» или «Найти устройство», система может скрывать их от сторонних приложений — это нормально для защиты приватности.",
                fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            )
        }

        // Расширенный список доверенных системных пакетов (Google, Huawei, Samsung, Xiaomi и т.д.)
        val known = setOf(
            "com.google.android.gms",
            "com.google.android.apps.adm",
            "com.huawei.android.finddevice",
            "com.huawei.hidisk", // Huawei Cloud
            "com.samsung.android.kgclient",
            "com.miui.securitycenter",
            "com.oppo.findmydevice",
            "com.vivo.finddevice"
        )

        val details = admins.map { "• ${it.packageName}" }
        val unknown = admins.filter { it.packageName !in known }

        val status = if (unknown.isEmpty()) CheckStatus.GREEN else CheckStatus.YELLOW
        val summary = if (unknown.isEmpty()) {
            "Активны только системные администраторы (безопасно)."
        } else {
            "Есть нестандартный администратор! Проверьте список ниже."
        }

        return CheckResult("admin", title, status, summary, details, Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
}