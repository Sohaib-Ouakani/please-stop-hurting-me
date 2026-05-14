package com.example.traveldiary.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LocationDisabledAlert(
    show: Boolean,
    onAction: () -> Unit,
    onHide: () -> Unit
) {
    if (show) {
        AlertDialog(
            title = { Text("Location disabled") },
            text = { Text("Location must be enabled to get your coordinates in the app.") },
            confirmButton = {
                TextButton(onClick = {
                    onAction()
                    onHide()
                }) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = onHide) {
                    Text("Dismiss")
                }
            },
            onDismissRequest = onHide
        )
    }
}

@Composable
fun PermissionDeniedAlert(
    show: Boolean,
    onAction: () -> Unit,
    onHide: () -> Unit
) {
    if (show) {
        AlertDialog(
            title = { Text("Location permission denied") },
            text = { Text("Location permission is required to get your coordinates in the app.") },
            confirmButton = {
                TextButton(onClick = {
                    onAction()
                    onHide()
                }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = onHide) {
                    Text("Dismiss")
                }
            },
            onDismissRequest = onHide
        )
    }
}

@Composable
fun PermissionPermanentlyDeniedSnackbar(
    snackbarHostState: SnackbarHostState,
    show: Boolean,
    onAction: () -> Unit,
    onHide: () -> Unit
) {
    if (show) {
        LaunchedEffect(snackbarHostState) {
            val res = snackbarHostState.showSnackbar(
                "Location permission is required.",
                "Go to Settings",
                duration = SnackbarDuration.Long
            )
            if (res == SnackbarResult.ActionPerformed) {
                onAction()
            }
            onHide()
        }
    }
}

@Composable
fun NoConnectivitySnackbar(
    snackbarHostState: SnackbarHostState,
    show: Boolean,
    onAction: () -> Unit,
    onHide: () -> Unit
) {
    if (show) {
        LaunchedEffect(snackbarHostState) {
            val res = snackbarHostState.showSnackbar(
                "No Internet connectivity.",
                "Go to Settings",
                duration = SnackbarDuration.Long
            )
            if (res == SnackbarResult.ActionPerformed) {
                onAction()
            }
            onHide()
        }
    }
}
