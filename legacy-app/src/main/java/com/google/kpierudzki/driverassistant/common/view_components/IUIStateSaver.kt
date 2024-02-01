package com.google.kpierudzki.driverassistant.common.view_components

import android.os.Bundle

interface IUIStateSaver<T> {
    fun roll(value: T, outState: Bundle)
    fun unroll(savedInstanceState: Bundle): T
}