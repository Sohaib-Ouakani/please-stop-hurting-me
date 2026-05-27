package com.example.corsa.ui.screens.rundetail

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.corsa.ui.composables.BackTopBar
import com.example.corsa.ui.theme.Spacing
import com.example.corsa.utils.formatDistance
import com.example.corsa.utils.formatDuration
import com.example.corsa.utils.formatPace
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import androidx.core.graphics.toColorInt
import org.maplibre.geojson.Polygon


private const val ROUTE_SOURCE_ID = "run-route-source"
private const val ROUTE_LAYER_ID  = "run-route-layer"
private const val MAP_STYLE_URL   = "https://tiles.openfreemap.org/styles/liberty"

// ── Fake comment model (replace with real data class later) ───────────────
data class Comment(
    val id: String,
    val authorName: String,
    val text: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunDetailScreen(
    navController: NavController,
    viewModel: RunDetailViewModel
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    when (val state = uiState) {
        is RunDetailUiState.Loading -> RunDetailLoading()
        is RunDetailUiState.Error   -> RunDetailError(message = state.message)
        is RunDetailUiState.Success -> {
            BottomSheetScaffold(
                scaffoldState = sheetState,
                topBar = { BackTopBar(navController = navController) },
                sheetPeekHeight = 260.dp,
                sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                sheetContainerColor = MaterialTheme.colorScheme.surface,
                sheetContent = {
                    RunDetailSheetContent(
                        state = state,
                        spacing = Spacing.md,
                        navController = navController
                    )
                }
            ) { paddingValues ->
                RunDetailMap(
                    geoJson = state.run.path,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

// ── Map ───────────────────────────────────────────────────────────────────

//@Composable
//fun RunDetailMap(
//    geoJson: String,
//    modifier: Modifier = Modifier
//) {
//    // TODO: replace Box with MapLibre MapboxMap composable and draw
//    //       the route using geoJsonToLineString(geoJson) as a LineLayer.
//
//    Box(
//        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = "Map placeholder",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}

@Composable
fun RunDetailMap(
    geoJson: String,
    modifier: Modifier = Modifier
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize MapLibre once per process — safe to call multiple times
    MapLibre.getInstance(context)

    val mapView = remember { MapView(context) }

    // Forward Compose lifecycle → MapView lifecycle callbacks
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner)   { mapView.onStart() }
            override fun onResume(owner: LifecycleOwner)  { mapView.onResume() }
            override fun onPause(owner: LifecycleOwner)   { mapView.onPause() }
            override fun onStop(owner: LifecycleOwner)    { mapView.onStop() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.getMapAsync { map ->
                map.setStyle(MAP_STYLE_URL) { style ->

                    // Guard: don't add source/layer if already present
                    if (style.getSource(ROUTE_SOURCE_ID) == null) {
                        val featureCollection = runCatching {
                            FeatureCollection.fromJson(geoJson).features()!![0].geometry() as Polygon
                        }.getOrNull()

                        val source = if (featureCollection != null) {
                            GeoJsonSource(ROUTE_SOURCE_ID, featureCollection)
                        } else {
                            GeoJsonSource(
                                ROUTE_SOURCE_ID,
                                Feature.fromGeometry(LineString.fromJson(geoJson))
                            )
                        }
                        style.addSource(source)
                    }

                    if (style.getLayer(ROUTE_LAYER_ID) == null) {
                        style.addLayer(
                            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                                PropertyFactory.lineColor("#E53935".toColorInt()),
                                PropertyFactory.lineWidth(4f),
                                PropertyFactory.lineCap("round"),
                                PropertyFactory.lineJoin("round")
                            )
                        )
                    }

                    // Camera fit — same as before
                    val coordinates = runCatching {
                        FeatureCollection.fromJson(geoJson)
                            .features()
                            ?.flatMap { feature ->
                                val geom = feature?.geometry()
                                if (geom is LineString) geom.coordinates() else emptyList()
                            }
                            ?.filterNotNull()
                    }.getOrNull()

                    if (!coordinates.isNullOrEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        coordinates.forEach { point ->
                            boundsBuilder.include(
                                org.maplibre.android.geometry.LatLng(point.latitude(), point.longitude())
                            )
                        }
                        runCatching { boundsBuilder.build() }.getOrNull()?.let { bounds ->
                            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
                        }
                    }
                }
            }
        }
    )
}

// ── Bottom sheet content ──────────────────────────────────────────────────

@Composable
fun RunDetailSheetContent(
    state: RunDetailUiState.Success,
    spacing: androidx.compose.ui.unit.Dp,
    navController: NavController
) {
    val run = state.run

    // Fake data — replace with real repo data later
    val fakeComments = listOf(
        Comment("1", "Alice", "Great pace on that last km!", "2h ago"),
        Comment("2", "Marco", "Bella corsa! 🔥", "3h ago"),
        Comment("3", "Sara",  "Keep it up!", "5h ago")
    )
    val fakeLikes = 14

    LazyColumn(
        contentPadding = PaddingValues(
            start = spacing,
            end = spacing,
            top = Spacing.sm,
            bottom = Spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {

        // ── User row + date ───────────────────────────────────────────────
        item {
            UserDateRow(
                userId = run.userId,
                startTime = run.startTime,
                navController = navController
            )
        }

        // ── Stat cards ────────────────────────────────────────────────────
        item {
            StatCardsGrid(run = run)
        }

        // ── Likes + comments label ────────────────────────────────────────
        item {
            LikesCommentsRow(
                likeCount = fakeLikes,
                commentCount = fakeComments.size
            )
        }

        // ── Comment divider ───────────────────────────────────────────────
        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // ── Comments ──────────────────────────────────────────────────────
        items(fakeComments, key = { it.id }) { comment ->
            CommentItem(comment = comment)
        }
    }
}

// ── User + date row ───────────────────────────────────────────────────────

@Composable
fun UserDateRow(
    userId: String,
    startTime: java.time.ZonedDateTime,
    navController: NavController
) {
    val dateLabel = startTime.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar placeholder — TODO: load real avatar via Coil
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                // TODO: replace userId with real username from profiles table
                text = userId.take(8) + "…",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // TODO: navigate to profile screen if userId != currentUserId,
        //       otherwise navigate to stats screen
        TextButton(onClick = { /* TODO navController.navigate(...) */ }) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Stat cards ────────────────────────────────────────────────────────────

@Composable
fun StatCardsGrid(run: com.example.corsa.data.model.Run) {
    // Build the list dynamically so optional stats appear only when present
    val stats = buildList {
        add("Distance"  to formatDistance(run.distanceMeters))
        add("Pace"       to formatPace(run.meanPaceSeconds))
        add("Duration"   to formatDuration(run.startTime, run.endTime))
        run.temperature?.let  { add("Temp"      to "%.1f °C".format(it)) }
        run.elevationGain?.let { add("Elevation" to "+%.0f m".format(it)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        stats.chunked(3).forEach { rowStats ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowStats.forEach { (label, value) ->
                    StatCard(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining slots in the last row so cards stay same width
                repeat(3 - rowStats.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.sm, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

// ── Likes + comments label row ────────────────────────────────────────────

@Composable
fun LikesCommentsRow(likeCount: Int, commentCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$commentCount comments",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Likes",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$likeCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Comment item ──────────────────────────────────────────────────────────

@Composable
fun CommentItem(comment: Comment) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorName.first().uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = comment.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Loading / Error states ────────────────────────────────────────────────

@Composable
fun RunDetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun RunDetailError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}