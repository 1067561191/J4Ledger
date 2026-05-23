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
    primary = Color(0xFF8FD3FF),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF164D69),
    onPrimaryContainer = Color(0xFFC9E6FF),
    secondary = Color(0xFFFFC857),
    onSecondary = Color(0xFF3D2F00),
    secondaryContainer = Color(0xFF5B4700),
    onSecondaryContainer = Color(0xFFFFE29B),
    tertiary = Color(0xFF95D5B2),
    onTertiary = Color(0xFF00391D),
    background = Color(0xFF111417),
    onBackground = Color(0xFFE2E3E6),
    surface = Color(0xFF171B1F),
    onSurface = Color(0xFFE2E3E6),
    surfaceVariant = Color(0xFF3F474E),
    onSurfaceVariant = Color(0xFFC0C7CE),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0B6E8F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6F3),
    onPrimaryContainer = Color(0xFF002F43),
    secondary = Color(0xFFB87300),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCA3),
    onSecondaryContainer = Color(0xFF3A2500),
    tertiary = Color(0xFF2F7D4F),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFB),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E7EC),
    onSurfaceVariant = Color(0xFF40484F),
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
