package com.example.securitychecker.checks

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object DeviceOwnerCheck {
    fun run(context: Context): CheckResult {
        val title = "Владелец устройства"
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val isDeviceOwner = try {
            val method = dpm.javaClass.getMethod("getDeviceOwner")
            method.invoke(dpm) != null
        } catch (_: Exception) {
            false
        }

        return if (!isDeviceOwner) {
            CheckResult("device_owner", title, CheckStatus.GREEN, "Приложение-владелец не обнаружено (это норма).")
        } else {
            CheckResult("device_owner", title, CheckStatus.RED, "Обнаружен Device Owner! Это огромный риск.", fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }
}