
package com.example.securitychecker.checks
import android.content.pm.PackageManager
import android.content.pm.PackageManager
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

        if (admins.isEmpty()) return CheckResult("admin", title, CheckStatus.GREEN, "Нет активных администраторов.", fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS))

        val known = setOf("com.google.android.gms", "com.google.android.apps.adm", "com.huawei.android.finddevice")
        val details = admins.map { "• ${it.loadLabel(context.packageManager)} (${it.packageName})" }
        val unknown = admins.filter { it.packageName !in known }

        val status = if (unknown.isEmpty()) CheckStatus.GREEN else CheckStatus.YELLOW
        val summary = if (unknown.isEmpty()) "Только системные администраторы." else "Есть нестандартный администратор!"

        return CheckResult("admin", title, status, summary, details, Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
}