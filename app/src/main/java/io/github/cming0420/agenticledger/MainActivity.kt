package io.github.cming0420.agenticledger

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.cming0420.agenticledger.theme.AgenticLedgerTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
      var showFirstLaunchDialog by remember {
        mutableStateOf(!prefs.getBoolean("has_seen_first_launch_dialog", false))
      }

      AgenticLedgerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation()
        }
      }

      if (showFirstLaunchDialog) {
        AlertDialog(
          onDismissRequest = {
            showFirstLaunchDialog = false
            prefs.edit().putBoolean("has_seen_first_launch_dialog", true).apply()
          },
          title = { Text("欢迎使用 AgenticLedger") },
          text = {
            Text(
              "本软件完全免费开源。\n\n" +
              "您的所有数据均保存在本地，不会上传至任何服务器。\n\n" +
              "如果您是通过付费渠道获得本软件，说明您已被骗，请立即报警。"
            )
          },
          confirmButton = {
            Button(onClick = {
              showFirstLaunchDialog = false
              prefs.edit().putBoolean("has_seen_first_launch_dialog", true).apply()
            }) {
              Text("我知道了")
            }
          },
        )
      }
    }
  }
}
