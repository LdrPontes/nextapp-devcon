package dev.ldrpontes.devcon

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ldrpontes.devcon.ui.theme.NexappDevConTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexappDevConTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StorageScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StorageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("devcon_prefs", Context.MODE_PRIVATE) }

    var inputText by remember { mutableStateOf("") }
    var savedValue by remember { mutableStateOf(prefs.getString("username", "") ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Native Android Storage", fontSize = 20.sp)

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter your username") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                prefs.edit().putString("username", inputText).apply()
                savedValue = inputText
                inputText = ""
            },
            enabled = inputText.isNotEmpty()
        ) {
            Text("Save")
        }

        if (savedValue.isNotEmpty()) {
            Text(text = "Stored: $savedValue", color = Color.Gray)
        }
    }
}
