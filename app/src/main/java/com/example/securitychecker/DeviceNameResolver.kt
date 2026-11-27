package com.example.securitychecker

import android.os.Build

object DeviceNameResolver {
    fun getMarketingName(): String {
        // Пытаемся прочитать скрытое системное свойство
        val marketName = getSystemProperty("ro.product.marketname")
        
        // Если свойство найдено и оно не похоже на технический код (без пробелов и строчных букв)
        if (!marketName.isNullOrBlank() && marketName.contains(" ")) {
            return marketName
        }

        // Если не получилось, возвращаем стандартную модель
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getSystemProperty(key: String): String? {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java)
            getMethod.invoke(null, key) as? String
        } catch (e: Exception) {
            null
        }
    }
}