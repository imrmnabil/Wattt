package com.bil.wattt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.bil.wattt.ui.theme.WatttTheme
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WatttTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WattageScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WattageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var wattage by remember { mutableDoubleStateOf(0.0) }

    DisposableEffect(context) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val voltageMilliVolts = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                               status == BatteryManager.BATTERY_STATUS_FULL
                
                // Watts = (Voltage in Volts) * (Current in Amperes)
                // If currentMicroAmps is negative, the battery is discharging.
                // If it's positive, it's charging (on most modern devices).
                // We'll show 0.00 W if not charging.
                val watts = if (isCharging) {
                    (voltageMilliVolts / 1000.0) * (abs(currentMicroAmps) / 1000000.0)
                } else {
                    0.0
                }
                wattage = watts
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format(Locale.US, "%.2f W", wattage),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
        )
    }
}