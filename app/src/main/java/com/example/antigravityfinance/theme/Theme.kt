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

private val DynamicDarkColorScheme = darkColorScheme(
    primary = DynamicDarkPrimary,
    secondary = DynamicDarkSecondary,
    tertiary = DynamicDarkTertiary,
    background = DynamicDarkBackground,
    surface = DynamicDarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFECE6F2),
    onSurface = Color(0xFFECE6F2),
    surfaceVariant = Color(0xFF2E1C4E),
    onSurfaceVariant = Color(0xFFE2D6F5)
)

private val DynamicLightColorScheme = lightColorScheme(
    primary = DynamicLightPrimary,
    secondary = DynamicLightSecondary,
    tertiary = DynamicLightTertiary,
    background = DynamicLightBackground,
    surface = DynamicLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1E0A2D),
    onSurface = Color(0xFF1E0A2D),
    surfaceVariant = Color(0xFFF0E4F8),
    onSurfaceVariant = Color(0xFF5A446A)
)

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary = ProfDarkPrimary,
    secondary = ProfDarkSecondary,
    tertiary = ProfDarkTertiary,
    background = ProfDarkBackground,
    surface = ProfDarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val ProfessionalLightColorScheme = lightColorScheme(
    primary = ProfLightPrimary,
    secondary = ProfLightSecondary,
    tertiary = ProfLightTertiary,
    background = ProfLightBackground,
    surface = ProfLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun AntigravityFinanceTheme(
    themeType: ThemeType = ThemeType.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    customAccent: Color? = null,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when (themeType) {
        ThemeType.DYNAMIC -> if (darkTheme) DynamicDarkColorScheme else DynamicLightColorScheme
        ThemeType.PROFESSIONAL -> if (darkTheme) ProfessionalDarkColorScheme else ProfessionalLightColorScheme
    }

    // Override primary with custom accent if specified
    val finalPrimary = customAccent ?: targetColorScheme.primary

    // Animate colors for smooth transitions
    val duration = 600
    val primary = animateColorAsState(targetValue = finalPrimary, animationSpec = tween(duration), label = "primary")
    val secondary = animateColorAsState(targetValue = targetColorScheme.secondary, animationSpec = tween(duration), label = "secondary")
    val tertiary = animateColorAsState(targetValue = targetColorScheme.tertiary, animationSpec = tween(duration), label = "tertiary")
    val background = animateColorAsState(targetValue = targetColorScheme.background, animationSpec = tween(duration), label = "background")
    val surface = animateColorAsState(targetValue = targetColorScheme.surface, animationSpec = tween(duration), label = "surface")
    val onPrimary = animateColorAsState(targetValue = targetColorScheme.onPrimary, animationSpec = tween(duration), label = "onPrimary")
    val onSecondary = animateColorAsState(targetValue = targetColorScheme.onSecondary, animationSpec = tween(duration), label = "onSecondary")
    val onTertiary = animateColorAsState(targetValue = targetColorScheme.onTertiary, animationSpec = tween(duration), label = "onTertiary")
    val onBackground = animateColorAsState(targetValue = targetColorScheme.onBackground, animationSpec = tween(duration), label = "onBackground")
    val onSurface = animateColorAsState(targetValue = targetColorScheme.onSurface, animationSpec = tween(duration), label = "onSurface")
    val surfaceVariant = animateColorAsState(targetValue = targetColorScheme.surfaceVariant, animationSpec = tween(duration), label = "surfaceVariant")
    val onSurfaceVariant = animateColorAsState(targetValue = targetColorScheme.onSurfaceVariant, animationSpec = tween(duration), label = "onSurfaceVariant")

    val animatedColorScheme = ColorScheme(
        primary = primary.value,
        onPrimary = onPrimary.value,
        primaryContainer = targetColorScheme.primaryContainer, // standard fallback
        onPrimaryContainer = targetColorScheme.onPrimaryContainer,
        inversePrimary = targetColorScheme.inversePrimary,
        secondary = secondary.value,
        onSecondary = onSecondary.value,
        secondaryContainer = targetColorScheme.secondaryContainer,
        onSecondaryContainer = targetColorScheme.onSecondaryContainer,
        tertiary = tertiary.value,
        onTertiary = onTertiary.value,
        tertiaryContainer = targetColorScheme.tertiaryContainer,
        onTertiaryContainer = targetColorScheme.onTertiaryContainer,
        background = background.value,
        onBackground = onBackground.value,
        surface = surface.value,
        onSurface = onSurface.value,
        surfaceVariant = surfaceVariant.value,
        onSurfaceVariant = onSurfaceVariant.value,
        surfaceTint = targetColorScheme.surfaceTint,
        inverseSurface = targetColorScheme.inverseSurface,
        inverseOnSurface = targetColorScheme.inverseOnSurface,
        error = targetColorScheme.error,
        onError = targetColorScheme.onError,
        errorContainer = targetColorScheme.errorContainer,
        onErrorContainer = targetColorScheme.onErrorContainer,
        outline = targetColorScheme.outline,
        outlineVariant = targetColorScheme.outlineVariant,
        scrim = targetColorScheme.scrim
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}

