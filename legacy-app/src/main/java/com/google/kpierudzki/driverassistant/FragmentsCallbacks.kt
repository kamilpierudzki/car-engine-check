package com.google.kpierudzki.driverassistant

import android.view.ViewGroup

/**
 * Created by Kamil on 14.03.2018.
 */
interface FragmentsCallbacks {
    fun getRootContainer(): ViewGroup
    fun setUseToolbarNavigationCustomAction(enabled: Boolean)
}

interface MainActivityFragmentsCallbacks : FragmentsCallbacks {
    fun onFragmentLoaded(loadedFragment: LoadedFragment)

    enum class LoadedFragment {
        EcoDriving,
        ObdII,
        History,
        Dtc
    }
}