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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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

val michromaFontFamily = FontFamily(
    Font(resId = R.font.michroma)
)

@Composable
fun WattageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var wattage by remember { mutableDoubleStateOf(0.0) }
    var lastDischargeCurrentMicroAmps by remember { mutableDoubleStateOf(0.0) }
    var dischargeStartTime by remember { mutableLongStateOf(0L) }
    var isCalibrated by remember { mutableStateOf(false) }
    var isChargingState by remember { mutableStateOf(false) }

    LaunchedEffect(isChargingState) {
        if (!isChargingState) {
            dischargeStartTime = System.currentTimeMillis()
            while (!isChargingState) {
                if (System.currentTimeMillis() - dischargeStartTime >= 5000) {
                    isCalibrated = true
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    DisposableEffect(context) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val voltageMilliVolts = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toDouble()
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || 
                               status == BatteryManager.BATTERY_STATUS_FULL)
                isChargingState = isCharging
                
                // If discharging, update our "offset" (system consumption)
                if (!isCharging && currentMicroAmps < 0) {
                    lastDischargeCurrentMicroAmps = abs(currentMicroAmps)
                }

                val voltageVolts = voltageMilliVolts / 1000.0
                val netCurrentAmps = abs(currentMicroAmps) / 1000000.0
                
                val watts = if (isCharging) {
                    val systemConsumptionAmps = lastDischargeCurrentMicroAmps / 1000000.0
                    voltageVolts * (netCurrentAmps + systemConsumptionAmps)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 60.sp)) {
                    if (isChargingState && !isCalibrated) {
                        append("N/A")
                    } else {
                        append(String.format(Locale.US, "%.2f", wattage))
                    }
                }
                withStyle(style = SpanStyle(fontSize = 30.sp)) {
                    append(" W")
                }
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = michromaFontFamily
                )
            )
        }

        // Message at the bottom to avoid shifting the center
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isChargingState && !isCalibrated) {
                Text(
                    text = "Remove background tasks and unplug for\nat least 5 seconds to calibrate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            } else if (!isChargingState && !isCalibrated) {
                Text(
                    text = "Calibrating...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = michromaFontFamily
                    )
                )
            }
        }
    }
}