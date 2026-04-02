package com.example.btbattery

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class BatteryWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.btbattery.ACTION_REFRESH"

        fun updateWidget(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, BatteryWidgetProvider::class.java)
            )
            for (id in ids) {
                updateSingleWidget(context, manager, id)
            }
        }

        private fun updateSingleWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_battery)

            // Get connected Bluetooth devices and their battery levels
            val devices = getConnectedDevices(context)

            // Build the device list view
            views.removeAllViews(R.id.deviceListContainer)

            if (devices.isEmpty()) {
                val emptyView = RemoteViews(context.packageName, R.layout.item_no_device)
                views.addView(R.id.deviceListContainer, emptyView)
            } else {
                for (device in devices) {
                    val deviceView = RemoteViews(context.packageName, R.layout.item_device)
                    deviceView.setTextViewText(R.id.tvDeviceName, device.name)
                    deviceView.setTextViewText(R.id.tvBatteryLevel, "${device.batteryLevel}%")
                    deviceView.setProgressBar(R.id.pbBattery, 100, device.batteryLevel, false)
                    deviceView.setImageViewResource(R.id.ivDeviceIcon, device.iconRes)
                    deviceView.setInt(
                        R.id.pbBattery,
                        "setProgressBackgroundTintList",
                        0xFFE0E0E0.toInt()
                    )

                    // Tint progress bar based on level
                    val color = when {
                        device.batteryLevel <= 20 -> 0xFFFF3B30.toInt() // Red
                        device.batteryLevel <= 50 -> 0xFFFF9500.toInt() // Orange
                        else -> 0xFF34C759.toInt() // Green
                    }
                    deviceView.setInt(R.id.pbBattery, "setProgressTintList", color)

                    views.addView(R.id.deviceListContainer, deviceView)
                }
            }

            // Update time
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            views.setTextViewText(R.id.tvUpdateTime, "更新于 ${sdf.format(Date())}")

            // Refresh button
            val refreshIntent = Intent(context, BatteryWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent)

            // Open app on click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPending = PendingIntent.getActivity(
                context, 1, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, openAppPending)

            manager.updateAppWidget(widgetId, views)
        }

        private fun getConnectedDevices(context: Context): List<BtDevice> {
            val devices = mutableListOf<BtDevice>()

            try {
                val bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = bluetoothManager.adapter ?: return emptyList()

                if (!adapter.isEnabled) return emptyList()

                val bondedDevices = adapter.bondedDevices ?: emptySet()

                for (device in bondedDevices) {
                    // Try to get connection state
                    val isConnected = try {
                        val method = adapter.javaClass.getMethod(
                            "getConnectionState",
                            device.javaClass
                        )
                        val state = method.invoke(adapter, device) as Int
                        state == BluetoothProfile.STATE_CONNECTED
                    } catch (e: Exception) {
                        // Fallback: check if device name suggests recent connection
                        true
                    }

                    if (isConnected) {
                        // Try to get battery level via reflection (Android 15+)
                        val batteryLevel = getBatteryLevel(device)

                        val iconRes = when {
                            device.name?.contains("Buds", ignoreCase = true) == true ||
                            device.name?.contains("AirPods", ignoreCase = true) == true ||
                            device.name?.contains("earbuds", ignoreCase = true) == true ||
                            device.name?.contains("耳机", ignoreCase = true) == true -> R.drawable.ic_earbuds

                            device.name?.contains("Watch", ignoreCase = true) == true ||
                            device.name?.contains("Band", ignoreCase = true) == true ||
                            device.name?.contains("手环", ignoreCase = true) == true -> R.drawable.ic_watch

                            device.name?.contains("Speaker", ignoreCase = true) == true ||
                            device.name?.contains("音箱", ignoreCase = true) == true -> R.drawable.ic_speaker

                            device.name?.contains("Keyboard", ignoreCase = true) == true ||
                            device.name?.contains("键盘", ignoreCase = true) == true -> R.drawable.ic_keyboard

                            device.name?.contains("Mouse", ignoreCase = true) == true ||
                            device.name?.contains("鼠标", ignoreCase = true) == true -> R.drawable.ic_mouse

                            else -> R.drawable.ic_bluetooth_default
                        }

                        devices.add(
                            BtDevice(
                                name = device.name ?: "未知设备",
                                batteryLevel = batteryLevel,
                                iconRes = iconRes,
                                address = device.address
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted
            }

            return devices.sortedByDescending { it.batteryLevel }
        }

        private fun getBatteryLevel(device: android.bluetooth.BluetoothDevice): Int {
            return try {
                val method = device.javaClass.getMethod("getBatteryLevel")
                val level = method.invoke(device) as Int
                if (level in 0..100) level else -1
            } catch (e: Exception) {
                -1
            }
        }
    }

    data class BtDevice(
        val name: String,
        val batteryLevel: Int,
        val iconRes: Int,
        val address: String
    )

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray
    ) {
        for (id in widgetIds) {
            updateSingleWidget(context, manager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateWidget(context)
        }
    }
}
