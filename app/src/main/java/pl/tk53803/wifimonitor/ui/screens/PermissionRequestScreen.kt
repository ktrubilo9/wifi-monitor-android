package pl.tk53803.wifimonitor.ui.screens

import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import pl.tk53803.wifimonitor.ui.MainNavigation

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    onPermissionsGranted: () -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
    )

    var hasTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && !hasTriggered) {
            hasTriggered = true
            onPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Aby korzystać z aplikacji, musisz przyznać odpowiednie uprawnienia.")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { permissionsState.launchMultiplePermissionRequest() }
        ) {
            Text("Zezwól na uprawnienia")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (permissionsState.shouldShowRationale) {
            Text("Wymagane uprawnienia do prawidłowego działania aplikacji.")
        }
    }
}