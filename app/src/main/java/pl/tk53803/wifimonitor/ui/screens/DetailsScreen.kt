import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.tk53803.wifimonitor.ui.screens.WifiViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenTopAppBar(
    ssid: String,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(ssid, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun DetailsScreen(
    bssid: String,
    viewModel: WifiViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val wifiHistory by viewModel.getSmoothedByBssid(bssid).collectAsState(initial = emptyList())
    var showRssiPopup by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            DetailsScreenTopAppBar(
                ssid = wifiHistory.firstOrNull()?.ssid ?: "Unknown Network",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        if (wifiHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No data found", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val latest = wifiHistory.maxByOrNull { it.timestamp }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Network Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        InfoRow(label = "SSID", value = latest?.ssid ?: "Unknown")
                        InfoRow(label = "BSSID", value = latest?.bssid ?: "Unknown")
                        InfoRow(label = "Frequency", value = "${latest?.frequency} MHz")
                        InfoRow(label = "Link Speed", value = "${latest?.linkSpeed} Mbps")
                        InfoRow(label = "Current RSSI", value = "${latest?.rssi} dBm")
                        InfoRow(label = "Estimated Distance", value = "${"%.2f".format(latest?.estimatedDistance)} m")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("RSSI History", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    RssiChart(rssiValues = wifiHistory.map { it.rssi }, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Actions", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        showRssiPopup = true
                    }) {
                        Text("Rssi list")
                    }

                    OutlinedButton(onClick = {
                        coroutineScope.launch {
                            viewModel.deleteOldData(bssid)
                        }
                    }) {
                        Text("Clear")
                    }
                }
            }
        }
    }
    if (showRssiPopup) {
        RssiListPopup(
            rssiValues = wifiHistory.map { it.rssi },
            onDismiss = { showRssiPopup = false }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RssiChart(rssiValues: List<Int>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val trimmedRssiValues = rssiValues
        .takeLast(60)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = {
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                axisRight.isEnabled = false

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)

                axisLeft.apply {
                    axisMinimum = -100f
                    axisMaximum = -10f
                    granularity = 10f
                    setDrawGridLines(true)
                }
            }
        },
        update = { chart ->
            val entries = trimmedRssiValues.mapIndexed { i, rssi ->
                Entry(i.toFloat(), rssi.toFloat())
            }

            val dataSet = LineDataSet(entries, "RSSI").apply {
                color = Color.Red.toArgb()
                valueTextColor = Color.Black.toArgb()
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun RssiListPopup(
    rssiValues: List<Int>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(text = "RSSI History")
        },
        text = {
            Column(modifier = Modifier
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState())
            ) {
                rssiValues.forEachIndexed { index, rssi ->
                    Text(text = "[$index] RSSI: $rssi dBm")
                    HorizontalDivider()
                }
            }
        }
    )
}
