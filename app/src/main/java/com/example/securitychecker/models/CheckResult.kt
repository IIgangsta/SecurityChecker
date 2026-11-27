package com.example.securitychecker.models

import android.content.Intent

data class CheckResult(
    val id: String,
    val title: String,
    val status: CheckStatus,
    val summary: String,
    val details: List<String> = emptyList(),
    val fixIntent: Intent? = null,
    val fixLabel: String = "Исправить",
    val isIgnored: Boolean = false // Новое поле: пропущена ли проверка
)