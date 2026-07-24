package com.kamran.screentimetracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.Dimensions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val modelProducer = remember { CartesianChartModelProducer() }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.weeklyUsage) {
        if (uiState.weeklyUsage.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(uiState.weeklyUsage.map { it.timeMillis.toFloat() / (1000 * 60 * 60) })
                }
            }
        }
    }

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            background = ShapeComponent(
                fill = Fill(MaterialTheme.colorScheme.surface.toArgb()),
                shape = CorneredShape.Pill
            ),
            padding = Dimensions(8f, 4f, 8f, 4f)
        )
    )

    val markerVisibilityListener = remember(uiState.weeklyUsage) {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val index = targets.firstOrNull()?.x?.toInt() ?: -1
                selectedIndex = index
                if (index != -1) {
                    scope.launch {
                        val listIndex = (uiState.weeklyUsage.size - 1) - index
                        if (listIndex in 0 until uiState.weeklyUsage.size) {
                            listState.animateScrollToItem(listIndex)
                        }
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val index = targets.firstOrNull()?.x?.toInt() ?: -1
                if (index != selectedIndex) {
                    selectedIndex = index
                    if (index != -1) {
                        scope.launch {
                            val listIndex = (uiState.weeklyUsage.size - 1) - index
                            if (listIndex in 0 until uiState.weeklyUsage.size) {
                                listState.animateScrollToItem(listIndex)
                            }
                        }
                    }
                }
            }

            override fun onHidden(marker: CartesianMarker) {
                selectedIndex = -1
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Last 7 Days Usage (Hours)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
                        val startAxis = VerticalAxis.rememberStart()
                        val bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = CartesianValueFormatter { _, value, _ ->
                                uiState.weeklyUsage.getOrNull(value.toInt())?.dayLabel ?: ""
                            }
                        )
                        val columnLayer = rememberColumnCartesianLayer(
                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(
                                    fill = Fill(primaryColor),
                                    thickness = 16.dp,
                                    shape = CorneredShape.rounded(4)
                                )
                            )
                        )
                        val chart = rememberCartesianChart(
                            columnLayer,
                            startAxis = startAxis,
                            bottomAxis = bottomAxis,
                            marker = marker,
                            markerVisibilityListener = markerVisibilityListener
                        )

                        CartesianChartHost(
                            chart = chart,
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Summary List
            val reversedUsage = remember(uiState.weeklyUsage) { uiState.weeklyUsage.asReversed() }
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(reversedUsage) { index, day ->
                    val originalIndex = (uiState.weeklyUsage.size - 1) - index
                    val isSelected = originalIndex == selectedIndex
                    
                    val hours = day.timeMillis / (1000 * 60 * 60)
                    val minutes = (day.timeMillis / (1000 * 60)) % 60
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = day.dayLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            trailingContent = { 
                                Text(
                                    text = "${hours}h ${minutes}m",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                        if (index < reversedUsage.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
