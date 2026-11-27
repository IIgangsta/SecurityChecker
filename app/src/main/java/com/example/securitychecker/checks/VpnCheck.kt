package com.example.securitychecker.checks

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.example.securitychecker.models.CheckResult
import com.example.securitychecker.models.CheckStatus

object VpnCheck {
    fun run(context: Context): CheckResult {
        val title = "VPN / Прокси"
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return CheckResult("vpn", title, CheckStatus.GREEN, "Нет активного сетевого подключения.")

            val caps = cm.getNetworkCapabilities(network)
            val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

            if (!isVpn) {
                CheckResult("vpn", title, CheckStatus.GREEN, "VPN-подключение не обнаружено.")
            } else {
                CheckResult("vpn", title, CheckStatus.YELLOW, "Обнаружено активное VPN-подключение.", fixIntent = Intent(Settings.ACTION_VPN_SETTINGS))
            }
        } catch (e: Exception) {
            CheckResult("vpn", title, CheckStatus.YELLOW, "Не удалось проверить VPN: ${e.localizedMessage}")
        }
    }
}