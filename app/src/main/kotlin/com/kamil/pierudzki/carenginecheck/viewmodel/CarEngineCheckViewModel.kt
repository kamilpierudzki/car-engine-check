package com.kamil.pierudzki.carenginecheck.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CarEngineCheckViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
) : ViewModel(), DefaultLifecycleObserver {

    val bluetoothDevices = bluetoothManager.bluetoothDevices
    val requestBluetoothPermission = bluetoothManager.requestBluetoothPermission
    val bluetoothAdapterState = bluetoothManager.adapterState

    override fun onCleared() {
        bluetoothManager.destroy()
    }

    override fun onStart(owner: LifecycleOwner) {
        bluetoothManager.refreshBluetoothDevices()
    }

    override fun onStop(owner: LifecycleOwner) {
        // todo
    }

    fun refreshBluetoothDevices() {
        bluetoothManager.refreshBluetoothDevices()
    }
}