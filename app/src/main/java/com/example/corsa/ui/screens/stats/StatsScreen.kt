package com.example.corsa.ui.screens.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.corsa.ui.composables.BottomBar
import com.example.corsa.ui.composables.TopBar

enum class StatsTab(val label: String) {
    Rank("Classifica"),
    Feed("Feed")
}

@Composable
fun StatsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(StatsTab.Rank) }
    val tabs = StatsTab.entries
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) },
        floatingActionButton = { FloatingActionButton(
            onClick = {},
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Outlined.Add, "Add Friends")
        } }, floatingActionButtonPosition = FabPosition.Center
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            // ── first Space  ────────────────────────────────────────────────

            Spacer(Modifier.height(16.dp))
            // ── Hero headline ────────────────────────────────────────────────

            Text(
                text = "WEEKLY \n LEADERBOARD",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = cs.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 30.sp,
                lineHeight = 45.sp,
                textAlign = TextAlign.Center,
            )
            // ── Search bar  ────────────────────────────────────────────────

            //to do

            // ── Primary Row  ────────────────────────────────────────────────
            PrimaryTabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }

            when (selectedTab) {
                StatsTab.Rank -> Rank()
                StatsTab.Feed -> Feed()
            }
        }
    }
}

@Composable
fun Feed() {

}

enum class RankTab(val label: String) {
    Kilometers("Kilometri"),
    Level("Livello")
}

@Composable
fun Rank() {
    var rankSelectedTab by remember { mutableStateOf(RankTab.Kilometers) }
    val tabs = RankTab.entries

    Column {
        SecondaryTabRow(selectedTabIndex = tabs.indexOf(rankSelectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = rankSelectedTab == tab,
                    onClick = { rankSelectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        when (rankSelectedTab) {
            RankTab.Kilometers -> RankTab()
            RankTab.Level -> RankTab()
        }
    }
}

@Composable
fun RankTab() {

}