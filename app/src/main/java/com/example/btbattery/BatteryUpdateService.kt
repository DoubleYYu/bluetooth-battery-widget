package com.example.btbattery

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Background service for periodic Bluetooth battery updates.
 * Automatically refreshes widget when Bluetooth state changes.
 */
class BatteryUpdateService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        BatteryWidgetProvider.updateWidget(this)
        stopSelf()
        return START_NOT_STICKY
    }
}
