package com.kamil.pierudzki.carenginecheck

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.kamil.pierudzki.carenginecheck.ui.theme.CarenginecheckTheme
import com.kamil.pierudzki.carenginecheck.viewmodel.CarEngineCheckViewModel
import com.kamil.pierudzki.carenginecheck.viewmodel.Permission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val carEngineCheckViewModel: CarEngineCheckViewModel by viewModels()

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            carEngineCheckViewModel.refreshBluetoothDevices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(carEngineCheckViewModel)
        setupUi()
        observeBluetoothPermissionRequest()
    }

    private fun setupUi() {
        setContent {
            CarenginecheckTheme {
                com.kamil.pierudzki.carenginecheck.ui.CarEngineCheckApp(
                    carEngineCheckViewModel = carEngineCheckViewModel,
                )
            }
        }
    }

    private fun observeBluetoothPermissionRequest() {
        lifecycleScope.launch {
            carEngineCheckViewModel.requestBluetoothPermission.collectLatest { permission ->
                if (permission is Permission.Requested && !permission.event.consumed) {
                    permission.event.consumed = true
                    bluetoothPermissionLauncher.launch(permission.event.data)
                }
            }
        }
    }
}