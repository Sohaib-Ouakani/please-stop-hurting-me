package com.example.corsa.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.corsa.ui.CorsaRoute
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar

/**
 * STRIDE – Home Screen (pure View, no ViewModel wiring).
 *
 * Colour mapping used from MaterialTheme.colorScheme:
 *   primary          → lime accent (CTAs, active labels, pace text)
 *   onPrimary        → content on lime (black text/icons inside START button)
 *   background       → dark scaffold background
 *   onBackground     → primary text on background
 *   surface          → card background
 *   onSurface        → primary text on cards
 *   secondary        → subtle surface (icon button backgrounds, progress track)
 *   onSurfaceVariant → muted / secondary text and icons
 */
@Composable
fun HomeScreen(
    state: HomeState,
    navController: NavController
) {
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Location label ───────────────────────────────────────────────
            LocationLable(cs, state.locationName)

            Spacer(Modifier.height(16.dp))

            // ── Hero headline ────────────────────────────────────────────────
            Text(
                text = "READY TO\nMOVE?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = cs.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 56.sp,
                lineHeight = 58.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            // ── START button ─────────────────────────────────────────────────
            StartButton(cs) { navController.navigate(CorsaRoute.Home) }

            Spacer(Modifier.height(36.dp))

            // ── Goal card ────────────────────────────────────────────────────
            GoalCard(
                cs,
                state.goalKm,
                state.currentKm,
                state.progress
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ColumnScope.StartButton(
    cs: ColorScheme,
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(220.dp)
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .background(cs.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStartClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start",
                tint = cs.onPrimary,
                modifier = Modifier.size(52.dp),
            )
            Text(
                text = "START",
                color = cs.onPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                letterSpacing = 3.sp,
            )
        }
    }
}

@Composable
private fun LocationLable(cs: ColorScheme, locationName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(cs.primary)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = locationName,
            color = cs.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
        )
    }
}

@Composable
private fun GoalCard(
    cs: ColorScheme,
    goalKm: Double,
    currentKm: Double,
    progress: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "GOAL",
                    color = cs.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.2f".format(goalKm),
                        color = cs.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = "KM",
                        color = cs.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Text(
                    text = "%.2f KM".format(currentKm),
                    color = cs.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                LinearProgressIndicator(
                    progress = { progress.toFloat() }
                )
            }
        }
    }
}

//@Preview(
//    name = "STRIDE Home – Dark",
//    showBackground = true,
//    backgroundColor = 0xFF1A1A1A,
//    device = "spec:width=390dp,height=844dp,dpi=420",
//)
//@Composable
//fun StrideHomeScreenPreview() {
//    CorsaTheme(darkTheme = true, dynamicColor = false) {
//        HomeScreen()
//    }
//}


