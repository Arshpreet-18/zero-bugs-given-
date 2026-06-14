package com.example.antigravityfinance.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.antigravityfinance.data.model.ThemeType

private val DynamicLightColorScheme = lightColorScheme(
    primary             = DynamicLightPrimary,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFD6E4FF),
    onPrimaryContainer  = Color(0xFF001D4A),
    secondary           = DynamicLightSecondary,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFB7F5C8),
    onSecondaryContainer = Color(0xFF00210F),
    tertiary            = DynamicLightTertiary,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410001),
    background          = Color(0xFFFFFFFF),
    onBackground        = Color(0xFF1A1C1E),
    surface             = Color(0xFFFFFFFF),
    onSurface           = Color(0xFF1A1C1E),
    surfaceVariant      = Color(0xFFF1F3F5),
    onSurfaceVariant    = Color(0xFF44474E),
    outline             = Color(0xFFDDE1E7),
    outlineVariant      = Color(0xFFE8EAED),
    error               = Color(0xFFBA1A1A),
    onError             = Color.White,
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410001)
)

private val DynamicDarkColorScheme = darkColorScheme(
    primary             = DynamicDarkPrimary,
    onPrimary           = Color(0xFF003062),
    primaryContainer    = Color(0xFF00468A),
    onPrimaryContainer  = Color(0xFFD6E4FF),
    secondary           = DynamicDarkSecondary,
    onSecondary         = Color(0xFF00391A),
    secondaryContainer  = Color(0xFF005228),
    onSecondaryContainer = Color(0xFFB7F5C8),
    tertiary            = DynamicDarkTertiary,
    onTertiary          = Color(0xFF690005),
    tertiaryContainer   = Color(0xFF93000A),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background          = DynamicDarkBackground,
    onBackground        = Color(0xFFE2E2E6),
    surface             = DynamicDarkSurface,
    onSurface           = Color(0xFFE2E2E6),
    surfaceVariant      = Color(0xFF1E2530),
    onSurfaceVariant    = Color(0xFFC5C6CF),
    outline             = Color(0xFF2D3545),
    error               = Color(0xFFFFB4AB),
    onError             = Color(0xFF690005),
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6)
)

private val ProfessionalLightColorScheme = lightColorScheme(
    primary             = ProfLightPrimary,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFE2E8F0),
    onPrimaryContainer  = Color(0xFF0F172A),
    secondary           = ProfLightSecondary,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary            = ProfLightTertiary,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFE2E8F0),
    onTertiaryContainer = Color(0xFF1E293B),
    background          = ProfLightBackground,
    onBackground        = Color(0xFF0F172A),
    surface             = ProfLightSurface,
    onSurface           = Color(0xFF0F172A),
    surfaceVariant      = Color(0xFFF1F5F9),
    onSurfaceVariant    = Color(0xFF475569),
    outline             = Color(0xFFE2E8F0)
)

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary             = ProfDarkPrimary,
    onPrimary           = Color.Black,
    primaryContainer    = Color(0xFF1E3A5F),
    onPrimaryContainer  = Color(0xFFBAE6FD),
    secondary           = ProfDarkSecondary,
    onSecondary         = Color.Black,
    secondaryContainer  = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary            = ProfDarkTertiary,
    onTertiary          = Color.White,
    background          = ProfDarkBackground,
    onBackground        = Color(0xFFF1F5F9),
    surface             = ProfDarkSurface,
    onSurface           = Color(0xFFF1F5F9),
    surfaceVariant      = Color(0xFF334155),
    onSurfaceVariant    = Color(0xFFCBD5E1),
    outline             = Color(0xFF475569)
)

@Composable
fun AntigravityFinanceTheme(
    themeType: ThemeType = ThemeType.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    customAccent: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = when (themeType) {
        ThemeType.DYNAMIC      -> if (darkTheme) DynamicDarkColorScheme else DynamicLightColorScheme
        ThemeType.PROFESSIONAL -> if (darkTheme) ProfessionalDarkColorScheme else ProfessionalLightColorScheme
    }

    val finalPrimary = customAccent ?: baseScheme.primary

    val duration = 500
    val primary          = animateColorAsState(finalPrimary,                   animationSpec = tween(duration), label = "primary")
    val secondary        = animateColorAsState(baseScheme.secondary,           animationSpec = tween(duration), label = "secondary")
    val tertiary         = animateColorAsState(baseScheme.tertiary,            animationSpec = tween(duration), label = "tertiary")
    val background       = animateColorAsState(baseScheme.background,          animationSpec = tween(duration), label = "bg")
    val surface          = animateColorAsState(baseScheme.surface,             animationSpec = tween(duration), label = "surface")
    val onPrimary        = animateColorAsState(baseScheme.onPrimary,           animationSpec = tween(duration), label = "onPrimary")
    val onSecondary      = animateColorAsState(baseScheme.onSecondary,         animationSpec = tween(duration), label = "onSecondary")
    val onTertiary       = animateColorAsState(baseScheme.onTertiary,          animationSpec = tween(duration), label = "onTertiary")
    val onBackground     = animateColorAsState(baseScheme.onBackground,        animationSpec = tween(duration), label = "onBg")
    val onSurface        = animateColorAsState(baseScheme.onSurface,           animationSpec = tween(duration), label = "onSurface")
    val surfaceVariant   = animateColorAsState(baseScheme.surfaceVariant,      animationSpec = tween(duration), label = "surfaceVariant")
    val onSurfaceVariant = animateColorAsState(baseScheme.onSurfaceVariant,    animationSpec = tween(duration), label = "onSurfaceVariant")

    val animatedScheme = ColorScheme(
        primary                = primary.value,
        onPrimary              = onPrimary.value,
        primaryContainer       = baseScheme.primaryContainer,
        onPrimaryContainer     = baseScheme.onPrimaryContainer,
        inversePrimary         = baseScheme.inversePrimary,
        secondary              = secondary.value,
        onSecondary            = onSecondary.value,
        secondaryContainer     = baseScheme.secondaryContainer,
        onSecondaryContainer   = baseScheme.onSecondaryContainer,
        tertiary               = tertiary.value,
        onTertiary             = onTertiary.value,
        tertiaryContainer      = baseScheme.tertiaryContainer,
        onTertiaryContainer    = baseScheme.onTertiaryContainer,
        background             = background.value,
        onBackground           = onBackground.value,
        surface                = surface.value,
        onSurface              = onSurface.value,
        surfaceVariant         = surfaceVariant.value,
        onSurfaceVariant       = onSurfaceVariant.value,
        surfaceTint            = baseScheme.surfaceTint,
        inverseSurface         = baseScheme.inverseSurface,
        inverseOnSurface       = baseScheme.inverseOnSurface,
        error                  = baseScheme.error,
        onError                = baseScheme.onError,
        errorContainer         = baseScheme.errorContainer,
        onErrorContainer       = baseScheme.onErrorContainer,
        outline                = baseScheme.outline,
        outlineVariant         = baseScheme.outlineVariant,
        scrim                  = baseScheme.scrim
    )

    MaterialTheme(
        colorScheme = animatedScheme,
        typography  = Typography,
        content     = content
    )
}
