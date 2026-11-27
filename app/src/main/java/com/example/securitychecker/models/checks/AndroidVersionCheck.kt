package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object AndroidVersionCheck {
    fun run(context: Context): CheckResult {
        val title = "Версия Android"
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE

        return when {
            sdk < Build.VERSION_CODES.S -> CheckResult("version", title, CheckStatus.RED, "Android $release — устарел.", fixIntent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            sdk < Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> CheckResult("version", title, CheckStatus.YELLOW, "Android $release — не самая свежая.", fixIntent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
            else -> CheckResult("version", title, CheckStatus.GREEN, "Android $release — актуальная.", fixIntent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
        }
    }
}