package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object DangerousPermissionsCheck {
    private val dangerous = listOf(
        android.Manifest.permission.RECEIVE_SMS,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.SEND_SMS,
        android.Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    fun run(context: Context): CheckResult {
        val title = "Опасные разрешения"
        val pm = context.packageManager
        val hits = mutableListOf<String>()
        val details = mutableListOf<String>()

        for (pkg in pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if (pkg.packageName == context.packageName) continue
            val ai = pkg.applicationInfo ?: continue
            if ((ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue
            
            val perms = pkg.requestedPermissions ?: continue
            val bad = dangerous.filter { it in perms }
            val granted = bad.filter { pm.checkPermission(it, pkg.packageName) == PackageManager.PERMISSION_GRANTED }
            
            if (granted.isNotEmpty()) {
                hits += pkg.packageName
                details += "• ${ai.loadLabel(pm)} (${pkg.packageName})"
            }
        }

        val status = if (hits.isEmpty()) CheckStatus.GREEN else CheckStatus.YELLOW
        val summary = if (hits.isEmpty()) "У сторонних приложений нет опасных разрешений." else "Есть ${hits.size} прил. с опасными правами."

        return CheckResult("danger", title, status, summary, details, Intent(Settings.ACTION_APPLICATION_SETTINGS))
    }
}