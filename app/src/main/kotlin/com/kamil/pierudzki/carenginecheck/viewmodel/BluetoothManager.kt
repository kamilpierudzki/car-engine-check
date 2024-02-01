package com.kamil.pierudzki.carenginecheck.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kamil.pierudzki.carenginecheck.logic.BluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface BluetoothAdapterState {
    data object Enabled : BluetoothAdapterState
    data object Disabled : BluetoothAdapterState
}

sealed interface Permission {
    data class Requested(val event: SingleEvent<String>) : Permission
    data object NotRequested : Permission
}

@ViewModelScoped
class BluetoothManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bluetoothManager: android.bluetooth.BluetoothManager,
    private val permissionCheckManager: PermissionCheckManager,
) {

    private val _adapterState =
        MutableStateFlow<SingleEvent<BluetoothAdapterState>>(
            SingleEvent(BluetoothAdapterState.Disabled)
        )
    val adapterState = _adapterState.asStateFlow()

    private val _requestBluetoothPermission =
        MutableStateFlow<Permission>(Permission.NotRequested)
    val requestBluetoothPermission = _requestBluetoothPermission.asStateFlow()

    private val _bluetoothDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val bluetoothDevices: StateFlow<List<BluetoothDevice>> = _bluetoothDevices.asStateFlow()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_ON ->
                        _adapterState.value = SingleEvent(BluetoothAdapterState.Enabled)

                    BluetoothAdapter.STATE_OFF ->
                        _adapterState.value = SingleEvent(BluetoothAdapterState.Disabled)
                }
            }
        }
    }

    init {
        appContext.registerReceiver(
            bluetoothReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
        )
    }

    fun destroy() {
        appContext.unregisterReceiver(bluetoothReceiver)
    }

    @SuppressLint("MissingPermission")
    fun refreshBluetoothDevices() {
        if (permissionCheckManager.hasBluetoothPermission()) {
            _bluetoothDevices.value = bluetoothManager.adapter.bondedDevices
                .map { bluetoothDevice ->
                    BluetoothDevice(
                        name = bluetoothDevice.name,
                        mac = bluetoothDevice.address,
                        device = bluetoothDevice,
                    )
                }
        } else {
            _requestBluetoothPermission.value = Permission.Requested(
                SingleEvent(permissionCheckManager.getPermissionName()),
            )
        }
    }
}