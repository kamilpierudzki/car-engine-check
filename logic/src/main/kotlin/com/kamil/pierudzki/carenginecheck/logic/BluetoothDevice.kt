package com.kamil.pierudzki.carenginecheck.logic

import android.bluetooth.BluetoothDevice

data class BluetoothDevice(
    val name: String,
    val mac: String,
    val device: BluetoothDevice,
)
