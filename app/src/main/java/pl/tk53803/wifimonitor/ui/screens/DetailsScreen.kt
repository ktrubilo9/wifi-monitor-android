import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.tk53803.wifimonitor.ui.screens.WifiViewModel
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
    val trimmedRssiValues = rssiValues
        .takeLast(60)
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(10) { index ->
            val y = size.height * index / 9f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        if (trimmedRssiValues.isEmpty()) {
            return@Canvas
        }

        val path = Path()
        val xStep = if (trimmedRssiValues.size > 1) {
            size.width / (trimmedRssiValues.size - 1)
        } else {
            0f
        }

        trimmedRssiValues.forEachIndexed { index, rssi ->
            val normalized = (rssi.coerceIn(-100, -10) + 100) / 90f
            val point = Offset(
                x = index * xStep,
                y = size.height * (1f - normalized)
            )

            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f)
        )

        val lastRssi = trimmedRssiValues.last()
        val lastNormalized = (lastRssi.coerceIn(-100, -10) + 100) / 90f
        drawCircle(
            color = lineColor,
            radius = 6f,
            center = Offset(
                x = (trimmedRssiValues.lastIndex * xStep),
                y = size.height * (1f - lastNormalized)
            )
        )
    }
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
