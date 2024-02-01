package com.kamil.pierudzki.carenginecheck.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.kamil.pierudzki.carenginecheck.R
import com.kamil.pierudzki.carenginecheck.viewmodel.BluetoothAdapterState
import com.kamil.pierudzki.carenginecheck.viewmodel.SingleEvent

@Composable
fun WelcomeScreen(
    bluetoothAdapterState: SingleEvent<BluetoothAdapterState>,
    onBluetoothAdapterDisabled: () -> Unit,
) {
    if (bluetoothAdapterState.data is BluetoothAdapterState.Disabled && !bluetoothAdapterState.consumed) {
        bluetoothAdapterState.consumed = true
        onBluetoothAdapterDisabled()
    }

    Scaffold { contentPadding ->
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "app icon",
            modifier = Modifier.padding(contentPadding),
        )
    }
}

