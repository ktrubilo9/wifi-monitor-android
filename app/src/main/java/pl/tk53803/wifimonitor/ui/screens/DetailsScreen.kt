import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import pl.tk53803.wifimonitor.ui.screens.WifiViewModel
import androidx.compose.ui.graphics.Color
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.*

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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
    val wifiHistory by viewModel.getByBssid(bssid).collectAsState(initial = emptyList())

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
            val latest = wifiHistory.first()

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
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Network Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        InfoRow(label = "SSID", value = latest.ssid)
                        InfoRow(label = "BSSID", value = latest.bssid)
                        InfoRow(label = "Frequency", value = "${latest.frequency} MHz")
                        InfoRow(label = "Link Speed", value = "${latest.linkSpeed} Mbps")
                        InfoRow(label = "Current RSSI", value = "${latest.rssi} dBm")
                        InfoRow(label = "Estimated Distance", value = "$.2f".format(latest.estimatedDistance))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("RSSI History", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    RssiChart(rssiValues = wifiHistory.map { it.rssi }, modifier = Modifier.padding(8.dp))
                }
            }
        }
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
    val points = rssiValues.mapIndexed { index, value ->
        Point(index.toFloat(), value.toFloat())
    }

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .backgroundColor(MaterialTheme.colorScheme.surface)
        .steps(points.size.coerceAtMost(10))
        .labelData { i -> "$i" }
        .labelAndAxisLinePadding(10.dp)
        .axisLabelColor(MaterialTheme.colorScheme.onSurface)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(6)
        .labelAndAxisLinePadding(10.dp)
        .labelData { i -> "${-i * 10} dBm" }
        .axisLabelColor(MaterialTheme.colorScheme.onSurface)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(color = Color.Red),
                    intersectionPoint = IntersectionPoint(
                        color = Color.Red,
                        radius = 4.dp
                    ),
                    selectionHighlightPoint = SelectionHighlightPoint(
                        color = Color.Red,
                        radius = 6.dp
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = MaterialTheme.colorScheme.surface,
        paddingRight = 12.dp,
        paddingTop = 12.dp
    )

    LineChart(
        modifier = modifier,
        lineChartData = lineChartData
    )
}