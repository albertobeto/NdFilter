package com.ndfilter.ndfilter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndfilter.ndfilter.ui.theme.NdFilterTheme
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NdFilterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ) { innerPadding ->
                        NdCalculatorScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun NdCalculatorScreen(modifier: Modifier = Modifier) {
    var selectedStops by remember { mutableStateOf(setOf(3.0)) } // Start with 3 stops
    var isMultipleMode by remember { mutableStateOf(value = false) }
    val totalStops = selectedStops.sum()

    val shutterSpeeds = remember { getShutterSpeeds() }
    
    // Computed values for the right wheel
    val filteredSpeeds = remember(totalStops) {
        shutterSpeeds.map { baseSpeed ->
            val raw = baseSpeed * 2.0.pow(totalStops)
            if (raw <= 5.0) {
                // Find the nearest standard value from the existing list
                shutterSpeeds.minByOrNull { kotlin.math.abs(it - raw) } ?: raw
            } else {
                // Round to the nearest second
                raw.roundToInt().toDouble()
            }
        }
    }
    
    // Find the first index that reaches or exceeds the 15-minute limit
    val limitIndex = remember(filteredSpeeds) {
        val idx = filteredSpeeds.indexOfFirst { it > 900.0 }
        if (idx == -1) filteredSpeeds.size - 1 else idx
    }
    
    // Base index state
    var baseIndex by remember { mutableIntStateOf(shutterSpeeds.indexOf(1.0/100).coerceAtLeast(0)) }
    
    // Single shared scroll state for perfect synchronization
    val sharedScrollState = rememberLazyListState(initialFirstVisibleItemIndex = baseIndex)

    // Improved centering logic for selection
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 80.dp.toPx() }
    
    val centeredIndex by remember {
        derivedStateOf {
            val index = sharedScrollState.firstVisibleItemIndex
            val offset = sharedScrollState.firstVisibleItemScrollOffset
            if (offset > (itemHeightPx / 2)) index + 1 else index
        }
    }

    // Update baseIndex selection when scrolling settles or centers
    LaunchedEffect(centeredIndex) {
        baseIndex = centeredIndex
    }

    // Ensure baseIndex stays within the current limit when filters change
    LaunchedEffect(limitIndex) {
        if (baseIndex > limitIndex) {
            baseIndex = limitIndex
            sharedScrollState.scrollToItem(limitIndex)
        }
    }
    
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ND Filter Calculator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selector Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSelector(isMultiple = isMultipleMode) { 
                isMultipleMode = it
                if (!isMultipleMode && (selectedStops.size > 1)) {
                    // If switching to single, keep only the largest selected stop
                    selectedStops = setOfNotNull(selectedStops.maxOrNull())
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        NdMultiSelectRow(
            label = "",
            options = listOf(0.5, 1.0, 2.0),
            selected = selectedStops,
        ) { value ->
            selectedStops = if (isMultipleMode) {
                if (selectedStops.contains(value)) selectedStops - value else selectedStops + value
            } else {
                if (selectedStops.contains(value)) emptySet() else setOf(value)
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        NdMultiSelectRow(
            label = "",
            options = listOf(3.0, 5.0, 10.0),
            selected = selectedStops,
        ) { value ->
            selectedStops = if (isMultipleMode) {
                if (selectedStops.contains(value)) selectedStops - value else selectedStops + value
            } else {
                if (selectedStops.contains(value)) emptySet() else setOf(value)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val ndValue = 2.0.pow(totalStops).roundToInt()
                val stopsDisplay = if ((totalStops % 1.0) == 0.0) totalStops.toInt().toString() else totalStops.toString()
                Text(
                    text = "Total Reduction: ND$ndValue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$stopsDisplay Stops",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Shared Wheel Container
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Base Exposure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                // Transparent arrow to match the wheel's alignment
                Text("→", fontSize = 30.sp, color = Color.Transparent)

                Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Filtered Exposure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(15.dp))

            ShutterSpeedDualWheel(
                baseItems = shutterSpeeds.take(limitIndex + 1),
                filteredItems = filteredSpeeds.take(limitIndex + 1),
                state = sharedScrollState,
                centeredIndex = centeredIndex
            )
        }
    }
}

@Composable
fun ModeSelector(
    isMultiple: Boolean,
    onModeChange: (Boolean) -> Unit,
) {
    val width = 180.dp
    val buttonWidth = width / 2
    
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .height(40.dp)
            .width(width),
    ) {
        Box {
            // Sliding background
            val offset by animateDpAsState(targetValue = if (isMultiple) buttonWidth else 0.dp)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth)
                    .offset(x = offset),
            ) {}
            
            Row(modifier = Modifier.fillMaxSize()) {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onModeChange(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Single",
                        color = if (!isMultiple) MaterialTheme.colorScheme.onPrimary else Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onModeChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Multiple",
                        color = if (isMultiple) MaterialTheme.colorScheme.onPrimary else Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NdMultiSelectRow(
    label: String,
    options: List<Double>,
    selected: Set<Double>,
    onToggle: (Double) -> Unit
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { value ->
                val isSelected = selected.contains(value)
                OutlinedButton(
                    onClick = { onToggle(value) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    val displayValue = if ((value % 1.0) == 0.0) value.toInt().toString() else value.toString()
                    Text(
                        text = displayValue,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShutterSpeedDualWheel(
    baseItems: List<Double>,
    filteredItems: List<Double>,
    state: LazyListState,
    centeredIndex: Int
) {
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = state)

    Box(
        modifier = Modifier
            .height(350.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Shared Highlight middle item
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.small
        ) {}

        LazyColumn(
            state = state,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 135.dp)
        ) {
            items(baseItems.size) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isSelected = centeredIndex == index
                    
                    // Base Value
                    Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = formatDuration(baseItems[index]),
                            style = if (isSelected)
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 40.sp
                                )
                            else
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 24.sp
                                )
                        )
                    }

                    Text("→", fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isSelected) 1f else 0.2f))

                    // Filtered Value
                    Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = formatDuration(filteredItems[index]),
                            style = if (isSelected)
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 40.sp
                                )
                            else
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 24.sp
                                )
                        )
                    }
                }
            }
        }
    }
}

fun getShutterSpeeds(): List<Double> {
    return listOf(
        1/8000.0,
        1/6400.0,
        1/5000.0,
        1/4000.0,
        1/3200.0,
        1/2500.0,
        1/2000.0,
        1/1600.0,
        1/1250.0,
        1/1000.0,
        1/800.0,
        1/640.0,
        1/500.0,
        1/400.0,
        1/320.0,
        1/250.0,
        1/200.0,
        1/160.0,
        1/125.0,
        1/100.0,
        1/80.0,
        1/60.0,
        1/50.0,
        1/40.0,
        1/30.0,
        1/25.0,
        1/20.0,
        1/15.0,
        1/13.0,
        1/10.0,
        1/8.0,
        1/6.0,
        1/5.0,
        1/4.0,
        1/3.0,
        1/2.5,
        1/2.0,
        1/1.6,
        1/1.3,
        1.0,
        1.3,
        1.5,
        2.0,
        2.5,
        3.0,
        4.0,
        5.0,
        6.5,
        8.0,
        10.0,
        13.0,
        15.0,
        20.0,
        25.0,
        30.0,
        40.0,
        50.0,
        60.0,
        80.0,
        100.0,
        120.0,
        160.0,
        200.0,
        240.0 // 4 min
    )
}

fun formatDuration(seconds: Double): String {
    if (seconds > 900.0) return "15+ min"
    
    return when {
        seconds >= 0.99 -> {
            if (seconds < 60.0) {
                val rounded = (seconds * 10).roundToInt() / 10.0
                if (rounded == rounded.toInt().toDouble()) "${rounded.toInt()}\"" else "$rounded\""
            } else {
                val totalMinutes = (seconds / 60).toInt()
                val remainingSeconds = (seconds % 60).roundToInt()
                if (remainingSeconds == 0) "${totalMinutes}m" else "${totalMinutes}m ${remainingSeconds}s"
            }
        }
        else -> {
            // Force 1/x format for all values below 1 second
            val d = 1.0 / seconds
            val text = if (d >= 9.9) {
                d.roundToInt().toString()
            } else {
                val rounded = (d * 10).roundToInt() / 10.0
                if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString() else rounded.toString()
            }
            "1/$text"
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun NdCalculatorPreview() {
    NdFilterTheme {
        NdCalculatorScreen()
    }
}
