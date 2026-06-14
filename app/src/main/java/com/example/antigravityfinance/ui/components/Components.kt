package com.example.antigravityfinance.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravityfinance.data.model.Transaction
import com.example.antigravityfinance.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2

// --- SKELETON LOADER ---
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.15f),
                shape = shape
            )
    )
}

@Composable
fun DashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonLoader(modifier = Modifier.fillMaxWidth().height(160.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonLoader(modifier = Modifier.weight(1f).height(100.dp))
            SkeletonLoader(modifier = Modifier.weight(1f).height(100.dp))
        }
        SkeletonLoader(modifier = Modifier.fillMaxWidth().height(200.dp))
        SkeletonLoader(modifier = Modifier.fillMaxWidth().height(80.dp))
    }
}

// --- INTERACTIVE DONUT CHART (Category Breakdown) ---
data class PieChartInput(
    val color: Color,
    val value: Double,
    val description: String,
    val isHighlighted: Boolean = false
)

@Composable
fun AnimatedDonutChart(
    inputs: List<PieChartInput>,
    modifier: Modifier = Modifier,
    innerRadiusFraction: Float = 0.65f,
    currencySymbol: String = "₹"
) {
    val totalSum = inputs.sumOf { it.value }
    var activeIndex by remember { mutableStateOf(-1) }
    
    // Animation progress
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(inputs) {
        animateProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .pointerInput(inputs) {
                    detectTapGestures { offset ->
                        val canvasSize = size.width
                        val center = Offset(canvasSize / 2f, canvasSize / 2f)
                        val x = offset.x - center.x
                        val y = offset.y - center.y
                        
                        // Calculate angle in degrees (0 to 360)
                        var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
                        if (angle < 0) {
                            angle += 360.0
                        }
                        
                        // Adjust angle because drawArc starts at 0 degree (which is rightmost point, 3 o'clock)
                        // but atan2 0 starts at 3 o'clock too.
                        var currentAngle = 0f
                        var foundIndex = -1
                        for (i in inputs.indices) {
                            val sweepAngle = ((inputs[i].value / totalSum) * 360f).toFloat()
                            if (angle >= currentAngle && angle <= currentAngle + sweepAngle) {
                                foundIndex = i
                                break
                            }
                            currentAngle += sweepAngle
                        }
                        activeIndex = if (activeIndex == foundIndex) -1 else foundIndex
                    }
                }
        ) {
            val strokeWidth = 32.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val rect = Rect(
                offset = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f),
                size = Size(diameter, diameter)
            )

            var startAngle = 0f
            inputs.forEachIndexed { index, input ->
                val sweepAngle = ((input.value / totalSum) * 360f).toFloat() * animateProgress.value
                val isSelected = index == activeIndex
                val currentStroke = if (isSelected) strokeWidth * 1.25f else strokeWidth
                val scaleBrush = if (isSelected) {
                    Brush.radialGradient(
                        colors = listOf(input.color, input.color.copy(alpha = 0.8f)),
                        center = rect.center
                    )
                } else {
                    SolidColor(input.color)
                }

                drawArc(
                    brush = scaleBrush,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                )
                startAngle += ((input.value / totalSum) * 360f).toFloat()
            }
        }

        // Inside text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (activeIndex != -1 && activeIndex < inputs.size) {
                val active = inputs[activeIndex]
                Text(
                    text = active.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$currencySymbol${String.format("%,.0f", active.value)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = active.color,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${String.format("%.1f", (active.value / totalSum) * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    text = "Total Spend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "$currencySymbol${String.format("%,.0f", totalSum)}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// --- INTERACTIVE BAR CHART (Credit vs Debit) ---
@Composable
fun InteractiveBarChart(
    creditValues: List<Double>,
    debitValues: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val maxVal = maxOf(
        (creditValues.maxOrNull() ?: 1.0),
        (debitValues.maxOrNull() ?: 1.0)
    ).toFloat()

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(creditValues, debitValues) {
        animProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Credit vs Debit",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                labels.forEachIndexed { index, label ->
                    val credit = creditValues.getOrElse(index) { 0.0 }.toFloat()
                    val debit = debitValues.getOrElse(index) { 0.0 }.toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(140.dp)
                        ) {
                            // Credit Bar
                            val incHeightFraction = (credit / maxVal) * animProgress.value
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight(incHeightFraction.coerceAtLeast(0.02f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(AccentEmerald, AccentEmerald.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                            // Debit Bar
                            val expHeightFraction = (debit / maxVal) * animProgress.value
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight(expHeightFraction.coerceAtLeast(0.02f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.tertiary,
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// --- LINE CHART (Savings and Investments Trends) ---
@Composable
fun SleekLineChart(
    dataPoints: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val maxVal = (dataPoints.maxOrNull() ?: 1.0).toFloat()
    val minVal = (dataPoints.minOrNull() ?: 0.0).toFloat()
    val valRange = (maxVal - minVal).coerceAtLeast(1f)

    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animateProgress.animateTo(1f, animationSpec = tween(1200, easing = LinearOutSlowInEasing))
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance & Growth Projection",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (dataPoints.size < 2) return@Canvas
                
                val width = size.width
                val height = size.height
                val stepX = width / (dataPoints.size - 1)
                
                val points = dataPoints.mapIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value.toFloat() - minVal) / valRange) * height * animateProgress.value
                    Offset(x, y)
                }

                // Create stroke path
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                // Create fill path
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                // Draw gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AccentIndigo.copy(alpha = 0.3f),
                            AccentIndigo.copy(alpha = 0.0f)
                        )
                    )
                )

                // Draw line stroke
                drawPath(
                    path = strokePath,
                    color = AccentIndigo,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw point nodes
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = AccentIndigo,
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// --- DUPLICATE WARNING DIALOG ---
@Composable
fun DuplicateWarningDialog(
    newTx: Transaction,
    existingTx: Transaction,
    onConfirmAnyway: () -> Unit,
    onCancel: () -> Unit,
    currencySymbol: String = "₹"
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Potential Duplicate Detected",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This transaction has a similar amount, merchant, and timestamp to an existing entry. Please review details below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Compare side-by-side or stacked
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Field", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Text(text = "Existing", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, color = AccentEmerald)
                        Text(text = "New Scan", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Amount row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Amount", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$currencySymbol${existingTx.amount}", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium, color = AccentEmerald, fontWeight = FontWeight.SemiBold)
                        Text(text = "$currencySymbol${newTx.amount}", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    
                    // Merchant row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Merchant", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(text = existingTx.merchant, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Text(text = newTx.merchant, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }

                    // Date row
                    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Time", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(text = dateFormat.format(Date(existingTx.date)), modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall)
                        Text(text = dateFormat.format(Date(newTx.date)), modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Cancel Addition")
                    }
                    Button(
                        onClick = onConfirmAnyway,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Confirm Anyway")
                    }
                }
            }
        }
    }
}

// --- SECURE PIN KEYBOARD GATE ---
@Composable
fun PinKeyboardGate(
    pinLength: Int = 4,
    isPinSet: Boolean,
    pinError: Boolean,
    onPinSubmitted: (String) -> Unit,
    onFingerprintClick: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    
    LaunchedEffect(pinError) {
        if (pinError) {
            enteredPin = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 60.dp)
        ) {
            Text(
                text = "FINKLAR",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPinSet) "Enter PIN to Unlock" else "Create Security PIN",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Dot indicators
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                for (i in 0 until pinLength) {
                    val isFilled = i < enteredPin.length
                    val color = if (pinError) {
                        MaterialTheme.colorScheme.error
                    } else if (isFilled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, CircleShape)
                    )
                }
            }
            if (pinError) {
                Text(
                    text = "Incorrect PIN. Try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Keyboard grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("F", "0", "⌫") // F = Fingerprint
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        val isSpecial = key == "F" || key == "⌫"
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSpecial) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    when (key) {
                                        "⌫" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        }
                                        "F" -> {
                                            if (onFingerprintClick != null && isPinSet) {
                                                onFingerprintClick()
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < pinLength) {
                                                enteredPin += key
                                                if (enteredPin.length == pinLength) {
                                                    onPinSubmitted(enteredPin)
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = if (isSpecial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
