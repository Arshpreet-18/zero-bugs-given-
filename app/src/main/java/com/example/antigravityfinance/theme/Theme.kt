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
    primary             = AppPrimaryColor,
    onPrimary           = Color.Black,
    primaryContainer    = Color(0xFFE5F5F8),
    onPrimaryContainer  = Color.Black,
    secondary           = AppPrimaryColor,
    onSecondary         = Color.Black,
    secondaryContainer  = Color(0xFFE5F5F8),
    onSecondaryContainer = Color.Black,
    tertiary            = AppPrimaryColor,
    onTertiary          = Color.Black,
    tertiaryContainer   = Color(0xFFE5F5F8),
    onTertiaryContainer = Color.Black,
    background          = Color.White,
    onBackground        = Color.Black,
    surface             = Color.White,
    onSurface           = Color.Black,
    surfaceVariant      = Color(0xFFF5F7F8),
    onSurfaceVariant    = Color.Black,
    outline             = Color(0xFFCCCCCC),
    outlineVariant      = Color(0xFFE5E5E5),
    error               = Color(0xFFBA1A1A),
    onError             = Color.White,
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410001)
)

private val DynamicDarkColorScheme = DynamicLightColorScheme

private val ProfessionalLightColorScheme = DynamicLightColorScheme

private val ProfessionalDarkColorScheme = DynamicLightColorScheme

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
