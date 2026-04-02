package com.example.btbattery

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Listens for Bluetooth connection changes and auto-refreshes the widget.
 */
class BluetoothStateReceiver : BroadcastReceiver() {

    companion object {
        fun register(context: Context) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED)
                addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            }
            context.registerReceiver(BluetoothStateReceiver(), filter)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED,
            BluetoothDevice.ACTION_ACL_DISCONNECTED,
            BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED,
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED" -> {
                // Auto-refresh widget when Bluetooth state changes
                BatteryWidgetProvider.updateWidget(context)
            }
        }
    }
}
