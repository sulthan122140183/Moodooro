package com.example.moodooro.ui.theme // Ganti dengan package Anda

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Atur default Typography Material 3 yang bisa Anda kustomisasi nanti
// Anda bisa menambahkan font kustom di sini jika sudah menambahkannya ke res/font
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    /*
    Anda bisa menambahkan atau mengoverride gaya teks lain sesuai kebutuhan Material 3:
    displayLarge, displayMedium, displaySmall,
    headlineLarge, headlineMedium, headlineSmall,
    titleLarge, titleMedium, titleSmall,
    bodyLarge, bodyMedium, bodySmall,
    labelLarge, labelMedium, labelSmall
    */
)