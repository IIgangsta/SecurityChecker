package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object DeveloperOptionsCheck {
    fun run(context: Context): CheckResult {
        val title = "Режим разработчика"
        val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

        return if (!adbEnabled) {
            CheckResult("devopts", title, CheckStatus.GREEN, "Отладка по USB отключена.")
        } else {
            CheckResult("devopts", title, CheckStatus.YELLOW, "Отладка по USB включена.", fixIntent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
    }
}