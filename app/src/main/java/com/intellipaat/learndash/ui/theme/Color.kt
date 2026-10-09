package com.intellipaat.learndash.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Indigo primary, warm amber progress accents. Restrained on purpose —
// the brief says skip fancy UI, so one accent + clean type does the job.
val IndigoPrimary = Color(0xFF3F51B5)
val IndigoDark = Color(0xFF2F3D8B)
val IndigoSoft = Color(0xFFE4E8FB)
val AmberProgress = Color(0xFFF59E0B)
val InkText = Color(0xFF1A1C22)
val MutedText = Color(0xFF6B7280)
val PageBg = Color(0xFFF5F6FA)
val CardBg = Color(0xFFFFFFFF)
val DangerRed = Color(0xFFD92D20)

val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoSoft,
    onPrimaryContainer = IndigoDark,
    secondary = AmberProgress,
    background = PageBg,
    onBackground = InkText,
    surface = CardBg,
    onSurface = InkText,
    surfaceVariant = Color(0xFFEBEDF3),
    onSurfaceVariant = MutedText,
    error = DangerRed
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF9FAEFB),
    onPrimary = Color(0xFF1A2150),
    primaryContainer = Color(0xFF2A3270),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = AmberProgress,
    background = Color(0xFF12141B),
    onBackground = Color(0xFFE9EBF2),
    surface = Color(0xFF1B1E28),
    onSurface = Color(0xFFE9EBF2),
    surfaceVariant = Color(0xFF2A2F3E),
    onSurfaceVariant = Color(0xFFA9B0C2),
    error = Color(0xFFF97066)
)
