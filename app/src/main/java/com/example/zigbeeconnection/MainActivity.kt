package com.example.zigbeeconnection

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.zigbeeconnection.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.HexDump
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var driver: UsbSerialDriver
    private lateinit var manager: UsbManager
    private lateinit var port: UsbSerialPort
    private var isConnected by Delegates.notNull<Boolean>()

    // Метод который выполняет во время создания activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isConnected = false
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, readFromDevice(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    // Ресивер запроса на коммуникацию с USB портом
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)) {
                        val connection = manager.openDevice(driver.device)

                        port = driver.ports[0]

                        Log.i("Open connection", port.toString())
                        port.open(connection)
                        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

                        isConnected = true

                        Log.e("RequestPermission", "permission granted")
                    } else {
                        Log.d("RequestPermission", "permission denied")
                    }
                }
            }
        }
    }

    // Читаем байты с девайса
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun readFromDevice(): String {
        manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers: List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        if (availableDrivers.isEmpty()) {
            Log.e("DeviceError", "Devices not found")
        }

        driver = availableDrivers[0]

        if (!isConnected) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )

            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            manager.requestPermission(driver.device, permissionIntent)
        }

        if (isConnected) {
            Log.i("Set Params", port.toString())

            val response = ByteArray(1024)
            port.read(response, 1000);
            Log.e("Response", HexDump.dumpHexString(response))

            port.write("0xf1".toByteArray(), 1000)

            val response2 = ByteArray(8)
            port.read(response2, 1000)
            Log.e("Response", HexDump.dumpHexString(response2))

            return HexDump.dumpHexString(response)
        }

        return "Permissions was requested. Please - retry"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}