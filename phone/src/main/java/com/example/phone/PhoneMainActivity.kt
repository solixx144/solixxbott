package com.example.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.phone.ui.PhoneMainScreen
import com.example.phone.ui.PhoneViewModel
import com.example.phone.ui.theme.SolixxBotTheme

class PhoneMainActivity : ComponentActivity() {
    private val viewModel: PhoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolixxBotTheme {
                PhoneMainScreen(viewModel = viewModel)
            }
        }
    }
}
