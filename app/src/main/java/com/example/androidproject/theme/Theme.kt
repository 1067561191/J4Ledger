package com.example.androidproject.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.androidproject.data.AppThemeMode

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD8ECFF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF073B36),
    secondaryContainer = Color(0xFF174E49),
    onSecondaryContainer = Color(0xFFCFFCF3),
    tertiary = Color(0xFFFCD34D),
    onTertiary = Color(0xFF423100),
    tertiaryContainer = Color(0xFF5D4707),
    onTertiaryContainer = Color(0xFFFFECB0),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF4B1111),
    background = Color(0xFF101314),
    onBackground = Color(0xFFE6E8EA),
    surface = Color(0xFF171A1C),
    onSurface = Color(0xFFE6E8EA),
    surfaceVariant = Color(0xFF2B3033),
    onSurfaceVariant = Color(0xFFC3C8CC),
    outline = Color(0xFF65717A),
    outlineVariant = Color(0xFF364047),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF172554),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8C2),
    onTertiaryContainer = Color(0xFF3A2500),
    error = Color(0xFFDC2626),
    onError = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
  )

@Composable
fun J4LedgerTheme(
  themeMode: AppThemeMode = AppThemeMode.System,
  content: @Composable () -> Unit,
) {
  val darkTheme =
    when (themeMode) {
      AppThemeMode.System -> isSystemInDarkTheme()
      AppThemeMode.Light -> false
      AppThemeMode.Dark -> true
    }
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
