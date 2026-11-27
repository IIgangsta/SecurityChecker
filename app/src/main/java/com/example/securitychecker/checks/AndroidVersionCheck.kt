package com.example.securitychecker.checks

import android.content.Context
import android.os.Build
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object AndroidVersionCheck {
    fun run(context: Context): CheckResult {
        val title = "Версия Android"
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE

        return when {
            sdk < Build.VERSION_CODES.S -> CheckResult(
                "version", title, CheckStatus.YELLOW, 
                "Android $release (API $sdk) — версия старше Android 12. Поддержка производителя могла истечь. Будьте осторожнее с установкой APK из неизвестных источников."
            )
            sdk < Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> CheckResult(
                "version", title, CheckStatus.GREEN, 
                "Android $release (API $sdk) — поддерживаемая версия."
            )
            else -> CheckResult(
                "version", title, CheckStatus.GREEN, 
                "Android $release (API $sdk) — актуальная версия."
            )
        }
    }
}