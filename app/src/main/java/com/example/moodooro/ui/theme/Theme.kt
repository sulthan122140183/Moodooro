package com.example.moodooro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Warna dari Palet Baru
val PaletteDarkBlue = Color(0xFF2E5077)
val PaletteTeal = Color(0xFF44919B) // Perkiraan dari gambar
val PaletteLightTealMint = Color(0xFF79D1C3) // Perkiraan dari gambar
val PaletteOffWhite = Color(0xFFFAF6F0) // Perkiraan dari gambar

// Definisikan peran warna Material 3 berdasarkan palet baru
// Anda mungkin perlu menyesuaikan ini lebih lanjut, terutama untuk dark theme
// dan berbagai 'container' colors untuk kontras yang optimal.

private val AppLightColorScheme = lightColorScheme(
    primary = PaletteDarkBlue,
    onPrimary = PaletteOffWhite,
    primaryContainer = PaletteTeal, // Atau PaletteLightTealMint jika ingin lebih terang
    onPrimaryContainer = PaletteDarkBlue, // Atau PaletteOffWhite jika kontrasnya baik

    secondary = PaletteTeal,
    onSecondary = PaletteOffWhite,
    secondaryContainer = PaletteLightTealMint,
    onSecondaryContainer = PaletteDarkBlue,

    tertiary = PaletteLightTealMint, // Atau warna aksen lain jika ada
    onTertiary = PaletteDarkBlue,
    tertiaryContainer = PaletteOffWhite, // Atau varian lebih terang dari PaletteLightTealMint
    onTertiaryContainer = PaletteDarkBlue,

    error = Color(0xFFB00020), // Standar Material error color
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFFB00020),

    background = PaletteOffWhite,
    onBackground = PaletteDarkBlue,

    surface = PaletteOffWhite, // Bisa sama dengan background
    onSurface = PaletteDarkBlue,
    surfaceVariant = Color(0xFFE7E0EC), // Warna netral sedikit lebih gelap/terang dari surface
    onSurfaceVariant = PaletteDarkBlue,

    outline = PaletteTeal // Atau warna netral yang lebih lembut
)

// Untuk DarkColorScheme, Anda perlu hati-hati memilih warna agar kontrasnya baik
// dan enak dilihat di lingkungan gelap. Warna gelap dari palet Anda mungkin
// perlu digunakan secara berbeda.
private val AppDarkColorScheme = darkColorScheme(
    primary = PaletteTeal, // Teal mungkin lebih menonjol sebagai primary di dark mode
    onPrimary = PaletteDarkBlue, // Atau PaletteOffWhite jika PaletteTeal cukup gelap
    primaryContainer = PaletteDarkBlue,
    onPrimaryContainer = PaletteLightTealMint,

    secondary = PaletteLightTealMint,
    onSecondary = PaletteDarkBlue,
    secondaryContainer = PaletteDarkBlue, // Atau varian lebih gelap dari LightTealMint
    onSecondaryContainer = PaletteLightTealMint,

    tertiary = PaletteDarkBlue, // Atau warna aksen lain
    onTertiary = PaletteLightTealMint,
    tertiaryContainer = Color(0xFF223D5C), // Varian sangat gelap dari DarkBlue
    onTertiaryContainer = PaletteLightTealMint,

    error = Color(0xFFCF6679), // Standar Material dark error color
    onError = Color.Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFCD8DF),

    background = PaletteDarkBlue,
    onBackground = PaletteOffWhite,

    surface = PaletteDarkBlue,
    onSurface = PaletteOffWhite,
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = PaletteLightTealMint,

    outline = PaletteTeal
)

@Composable
fun MoodooroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android S+
    // Anda bisa set dynamicColor ke false jika ingin selalu menggunakan skema warna kustom ini
    dynamicColor: Boolean = false, // Set ke false untuk menggunakan skema kustom secara eksplisit
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan Anda memiliki Typography.kt yang terdefinisi
        shapes = Shapes,         // Pastikan Anda memiliki Shapes.kt yang terdefinisi
        content = content
    )
}