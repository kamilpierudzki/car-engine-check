package com.google.kpierudzki.driverassistant.ecoDriving.connector

import com.google.kpierudzki.driverassistant.common.connector.IPermissionRequirable
import com.google.kpierudzki.driverassistant.common.connector.IPersistableBuffer
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager

/**
 * Created by Kamil on 20.07.2017.
 */

interface IEcoDrivingObservable : IPersistableBuffer, IPermissionRequirable, IBaseManager.IBaseManagerObservable
