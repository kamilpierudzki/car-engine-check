package com.google.kpierudzki.driverassistant.obd.service.obdmanager

import com.google.kpierudzki.driverassistant.common.connector.IPermissionRequirable

/**
 * Created by Kamil on 16.09.2017.
 */

interface IObdCommonObservable : IPermissionRequirable {
    fun disconnect()
}
