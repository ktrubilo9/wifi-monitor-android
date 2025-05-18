package pl.tk53803.wifimonitor.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import pl.tk53803.wifimonitor.R
import pl.tk53803.wifimonitor.ui.screens.WifiViewModel

@Composable
fun HomeScreen(
    viewModel: WifiViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val wifilist by viewModel.wifiHistory.collectAsState()
    var expandedBssid by rememberSaveable { mutableStateOf<String?>(null) }

    val filteredList = remember(wifilist) {
        wifilist.distinctBy { it.bssid }
            .sortedByDescending { it.rssi }
    }

    Scaffold (
        modifier = Modifier.Companion.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { WifiMonitorTopAppBar() }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(
                items = filteredList,
                key= { it.bssid }
            ) { item ->
                val isExpanded = expandedBssid == item.bssid

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ArrowRotation"
                )


                Card(
                    onClick = { expandedBssid = if (isExpanded) null else item.bssid },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()

                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SSID: ${item.ssid}", style = MaterialTheme.typography.titleMedium)
                                Text("RSSI: ${item.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                                Text("Distance: ${item.estimatedDistance} m", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.rotate(rotation)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            InfoRow("BSSID", item.bssid)
                            InfoRow("Speed", "${item.linkSpeed} Mbps")
                            InfoRow("Frequency", "${item.frequency} MHz")

                            Spacer(modifier = Modifier.height(12.dp))

                            // Navigate Button
                            Button(
                                onClick = { onNavigateToDetail(item.bssid) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("View Details")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiMonitorTopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}