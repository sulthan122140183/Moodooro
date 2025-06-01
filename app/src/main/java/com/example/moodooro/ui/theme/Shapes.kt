package com.example.moodooro.ui.theme // Ganti dengan package Anda

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp), // Bentuk umum untuk Card, Button, dll.
    large = RoundedCornerShape(12.dp)  // Bentuk untuk komponen yang lebih besar atau dialog
)

/*
Anda bisa mendefinisikan bentuk yang lebih spesifik jika diperlukan:
extraSmall = RoundedCornerShape(percent = 50), // Untuk bentuk lingkaran penuh
extraLarge = RoundedCornerShape(0.dp)          // Untuk bentuk persegi tanpa lengkungan
*/