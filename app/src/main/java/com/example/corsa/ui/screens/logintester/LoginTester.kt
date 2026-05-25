package com.example.corsa.ui.screens.logintester

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import org.koin.androidx.compose.koinViewModel
import com.example.corsa.data.remote.supabase
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    val viewModel = koinViewModel<LoginTesterViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val authState = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> viewModel.onGoogleSignInSuccess()
                is NativeSignInResult.ClosedByUser -> viewModel.onSignInDismissed()
                is NativeSignInResult.Error -> {
                    Log.e("Auth", "Error: ${result.message}")
                    viewModel.onSignInError("Error: ${result.message}")
                }
                is NativeSignInResult.NetworkError -> {
                    Log.e("Auth", "Network error: ${result.message}")
                    viewModel.onSignInError("Network error: ${result.message}")
                }
            }
        }
    )

    // React to success state
    if (state is LoginState.Success) {
        onLoginSuccess()
        return
    }

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { authState.startFlow() }) {
                Text("Sign in with Google")
            }

            if (state is LoginState.Error) {
                Text(
                    text = (state as LoginState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}