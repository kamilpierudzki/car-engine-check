package com.kamil.pierudzki.carenginecheck.viewmodel

data class SingleEvent<T>(val data: T, var consumed: Boolean = false)
