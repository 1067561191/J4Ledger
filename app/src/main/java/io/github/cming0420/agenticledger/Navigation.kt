package io.github.cming0420.agenticledger

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.cming0420.agenticledger.ui.main.MainScreen
import io.github.cming0420.agenticledger.ui.main.TutorialScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onNavigateToTutorial = { backStack += Tutorial(it) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
          )
        }
        entry<Tutorial> { key ->
          TutorialScreen(
            type = key.type,
            onBack = { backStack.removeLastOrNull() },
          )
        }
      },
  )
}
