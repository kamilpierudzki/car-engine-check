package com.kamil.pierudzki.carenginecheck.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kamil.pierudzki.carenginecheck.logic.BluetoothDevice

@Composable
fun AvailableDevicesScreen(
    bluetoothDevices: List<BluetoothDevice>,
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
        ) {
            LazyColumn(content = {
                itemsIndexed(bluetoothDevices) { index, item ->
                    BluetoothDeviceItem(index = index, name = item.name, mac = item.mac)
                }
            })

        }
    }
}

@Composable
private fun BluetoothDeviceItem(index: Int, name: String, mac: String) {
    Row {
        Text(text = name)
        Text(text = mac)
    }
}