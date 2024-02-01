package com.kamil.pierudzki.carenginecheck.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamil.pierudzki.carenginecheck.ui.screen.AvailableDevicesScreen
import com.kamil.pierudzki.carenginecheck.ui.screen.BluetoothDisabledScreen
import com.kamil.pierudzki.carenginecheck.ui.screen.WelcomeScreen
import com.kamil.pierudzki.carenginecheck.viewmodel.CarEngineCheckViewModel

sealed class NavigationScreen(val name: String) {
    data object Welcome : NavigationScreen("welcomeScreen")
    data object AvailableDevices : NavigationScreen("availableDevicesScreen")
    data object BluetoothDisabled : NavigationScreen("bluetoothDisabledScreen")
    data object Connecting : NavigationScreen("connectingScreen")
    data object MissingPermission : NavigationScreen("missingPermissionScreen")
    data object Error : NavigationScreen("errorScreen")
}

@Composable
fun CarEngineCheckApp(
    carEngineCheckViewModel: CarEngineCheckViewModel,
) {
    val navController: NavHostController = rememberNavController()
    CarEngineCheckNavHost(
        navController = navController,
        carEngineCheckViewModel = carEngineCheckViewModel,
    )
}

@Composable
private fun CarEngineCheckNavHost(
    navController: NavHostController,
    carEngineCheckViewModel: CarEngineCheckViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.Welcome.name,
    ) {
        composable(NavigationScreen.Welcome.name) {
            WelcomeScreen(
                bluetoothAdapterState = carEngineCheckViewModel.bluetoothAdapterState.collectAsStateWithLifecycle().value,
                onBluetoothAdapterDisabled = {
                    navController.navigate(NavigationScreen.BluetoothDisabled.name)
                }
            )
        }
        composable(NavigationScreen.AvailableDevices.name) {
            AvailableDevicesScreen(
                bluetoothDevices = carEngineCheckViewModel.bluetoothDevices.collectAsStateWithLifecycle().value,
            )
        }
        composable(NavigationScreen.BluetoothDisabled.name) {
            BluetoothDisabledScreen()
        }
        composable(NavigationScreen.Connecting.name) {
            // todo
        }
        composable(NavigationScreen.MissingPermission.name) {
            // todo
        }
        composable(NavigationScreen.Error.name) {
            // todo
        }
    }
}