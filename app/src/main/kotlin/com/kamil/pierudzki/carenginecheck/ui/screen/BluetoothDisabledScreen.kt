package com.kamil.pierudzki.carenginecheck.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BluetoothDisabledScreen() {
    Scaffold { contentPadding ->
        Text(
            text = "Bluetooth is disabled",
            modifier = Modifier.padding(contentPadding),
        )
    }
}