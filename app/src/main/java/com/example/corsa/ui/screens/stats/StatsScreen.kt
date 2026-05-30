package com.example.corsa.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.corsa.data.model.Run
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.ProfileStats
import com.example.corsa.ui.composables.TopBar
import com.example.corsa.ui.composables.UserEntry
import com.example.corsa.ui.theme.Size
import com.example.corsa.ui.theme.Spacing


@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsScreenViewModel
) {
    val user by viewModel.profile.collectAsStateWithLifecycle()
    val runs by viewModel.runs.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            viewModel.refreshProfile()
        }
    }
    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) },
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            if (user != null) {
                ProfileStats(
                    navController,
                    runs,
                    user!!,
                    { ProfileHeader(user!!, cs) }
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    infoEntries: UserEntry,
    cs: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, // nome a sx, avatar a dx
    ) {
        Text(
            text = infoEntries.displayName,
            color = cs.onSurface,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.weight(1f), // occupa lo spazio senza spingere l'avatar
        )
        Spacer(Modifier.width(Spacing.md))
        Box(
            modifier = Modifier
                .size(Size.l)
                .clip(CircleShape)
                .background(cs.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = infoEntries.displayName.first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}