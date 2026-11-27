package com.example.securitychecker.checks

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object ScreenLockCheck {
    fun run(context: Context): CheckResult {
        val title = "Блокировка экрана"
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        return if (km.isDeviceSecure) {
            CheckResult("screenlock", title, CheckStatus.GREEN, "Экран защищен паролем или PIN-кодом.")
        } else {
            CheckResult("screenlock", title, CheckStatus.RED, "Экран не защищен паролем.", fixIntent = Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }
}