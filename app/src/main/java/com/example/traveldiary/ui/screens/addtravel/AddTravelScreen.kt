package com.example.traveldiary.ui.screens.addtravel

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.http.data.OSMDataSource
import com.example.traveldiary.data.LocationService
import com.example.traveldiary.ui.composables.AppBar
import com.example.traveldiary.ui.composables.ImageWithPlaceholder
import com.example.traveldiary.ui.composables.LocationDisabledAlert
import com.example.traveldiary.ui.composables.NoConnectivitySnackbar
import com.example.traveldiary.ui.composables.PermissionDeniedAlert
import com.example.traveldiary.ui.composables.PermissionPermanentlyDeniedSnackbar
import com.example.traveldiary.ui.composables.Size
import com.example.traveldiary.utils.PermissionStatus
import com.example.traveldiary.utils.isOnline
import com.example.traveldiary.utils.openAppSettings
import com.example.traveldiary.utils.openLocationSettings
import com.example.traveldiary.utils.openWirelessSettings
import com.example.traveldiary.utils.rememberCameraLauncher
import com.example.traveldiary.utils.rememberMultiplePermissions
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AddTravelScreen(
    state: AddTravelState,
    actions: AddTravelActions,
    navController: NavHostController
) {
    val ctx = LocalContext.current

    val (_, takePicture) = rememberCameraLauncher(
        onPictureTaken = { imageUri -> actions.setImageUri(imageUri) }
    )

    val locationService = koinInject<LocationService>()
    val osmDataSource = koinInject<OSMDataSource>()

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { statuses ->
        when {
            statuses.any { it.value == PermissionStatus.Granted } -> {}
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } ->
                actions.setShowPermissionPermanentlyDeniedSnackbar(true)
            else ->
                actions.setShowPermissionDeniedAlert(true)
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun getCurrentLocationName() = scope.launch {
        if (locationPermissions.statuses.none { it.value.isGranted }) {
            locationPermissions.launchPermissionRequest()
            return@launch
        }
        val coordinates = try {
            locationService.getCurrentLocation() ?: return@launch
        } catch (_: IllegalStateException) {
            actions.setShowLocationDisabledAlert(true)
            return@launch
        }
        if (!isOnline(ctx)) {
            actions.setShowNoConnectivitySnackbar(true)
            return@launch
        }
        val place = osmDataSource.getPlace(coordinates)
        actions.setDestination(place.displayName)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { AppBar("Add Travel", navController, showSearchButton = false) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    if (!state.canSubmit) return@FloatingActionButton
                    actions.submit()
                    navController.navigateUp()
                }
            ) {
                Icon(Icons.Outlined.Check, "Add Travel")
            }
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.destination,
                onValueChange = actions.setDestination,
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    getCurrentLocationName().join()
                                    isLoading = false
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.MyLocation, "Current location")
                        }
                    }
                }
            )
            OutlinedTextField(
                value = state.date,
                onValueChange = actions.setDate,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = actions.setDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(24.dp))
            Button(
                onClick = takePicture,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = "Camera icon",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Take a picture")
            }
            Spacer(Modifier.size(8.dp))
            ImageWithPlaceholder(state.imageUri, Size.Lg)
        }

        LocationDisabledAlert(
            show = state.showLocationDisabledAlert,
            onAction = { openLocationSettings(ctx) },
            onHide = { actions.setShowLocationDisabledAlert(false) }
        )

        PermissionDeniedAlert(
            show = state.showPermissionDeniedAlert,
            onAction = locationPermissions::launchPermissionRequest,
            onHide = { actions.setShowPermissionDeniedAlert(false) }
        )

        PermissionPermanentlyDeniedSnackbar(
            snackbarHostState,
            show = state.showPermissionPermanentlyDeniedSnackbar,
            onAction = { openAppSettings(ctx) },
            onHide = { actions.setShowPermissionPermanentlyDeniedSnackbar(false) }
        )

        NoConnectivitySnackbar(
            snackbarHostState,
            show = state.showNoConnectivitySnackbar,
            onAction = { openWirelessSettings(ctx) },
            onHide = { actions.setShowNoConnectivitySnackbar(false) }
        )
    }
}
