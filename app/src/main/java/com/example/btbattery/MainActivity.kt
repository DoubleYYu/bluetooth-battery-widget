package com.example.btbattery

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.btbattery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_PERMISSIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        setupUI()
        refreshWidget()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissions.add(Manifest.permission.BLUETOOTH)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    private fun setupUI() {
        // Show connected devices info
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter == null) {
            binding.tvStatus.text = "此设备不支持蓝牙"
            return
        }

        if (!adapter.isEnabled) {
            binding.tvStatus.text = "蓝牙未开启"
            binding.btnEnableBt.setOnClickListener {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        } else {
            binding.tvStatus.text = "蓝牙已开启"
            refreshWidget()
        }

        binding.btnRefresh.setOnClickListener {
            refreshWidget()
            Toast.makeText(this, "小部件已刷新", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshWidget() {
        BatteryWidgetProvider.updateWidget(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                refreshWidget()
            } else {
                Toast.makeText(this, "需要蓝牙权限才能查看设备电量", Toast.LENGTH_LONG).show()
            }
        }
    }
}
