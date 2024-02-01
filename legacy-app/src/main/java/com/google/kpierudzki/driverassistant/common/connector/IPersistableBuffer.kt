package com.google.kpierudzki.driverassistant.common.connector

interface IPersistableBuffer {
    fun forcePersistBuffer(async: Boolean)
}