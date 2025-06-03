package pl.tk53803.wifimonitor.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
    )

    var hasRequestedPermissionsBefore by remember { mutableStateOf(false) }

    var hasTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && !hasTriggered) {
            hasTriggered = true
            onPermissionsGranted()
        }
    }

    val permanentlyDenied = hasRequestedPermissionsBefore &&
            permissionsState.permissions.any {
                !it.status.isGranted && !it.status.shouldShowRationale
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
            onClick = {
                hasRequestedPermissionsBefore = true
                permissionsState.launchMultiplePermissionRequest()
            },
            enabled = !permanentlyDenied
        ) {
            Text("Zezwól na uprawnienia")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (permissionsState.shouldShowRationale) {
            Text("Wymagane uprawnienia do prawidłowego działania aplikacji.")
        }

        if (permanentlyDenied) {
            Text("Niektóre uprawnienia zostały trwale odrzucone. Otwórz ustawienia, aby je ręcznie przyznać.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { openAppSettings() }) {
                Text("Otwórz ustawienia aplikacji")
            }
        }
    }
}
