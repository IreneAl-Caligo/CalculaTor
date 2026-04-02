package com.calculator.nur.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculator.nur.viewmodel.CalculatorViewModel
import com.calculator.nur.ui.theme.AccentColor

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val history by viewModel.history.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { /* handle end */ }
                        ) { change, dragAmount ->
                            change.consume()
                            if (dragAmount.x < -20 || dragAmount.x > 20) {
                                viewModel.deleteAtCursor()
                            }
                        }
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    
                    Text(
                        text = "X: ${viewModel.varX ?: 0}  Y: ${viewModel.varY ?: 0}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (viewModel.cursorPosition > 0) {
                            Text(
                                "◀", 
                                modifier = Modifier
                                    .clickable { viewModel.moveCursorLeft() }
                                    .padding(8.dp), 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Expression view with cursor
                        val expr = viewModel.expression
                        val cursorHtml = buildString {
                            append(expr.substring(0, viewModel.cursorPosition))
                            append("|")
                            append(expr.substring(viewModel.cursorPosition))
                        }
                        
                        Text(
                            text = cursorHtml,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.End,
                            maxLines = 2
                        )
                        
                        if (viewModel.cursorPosition < viewModel.expression.length) {
                             Text(
                                "▶", 
                                modifier = Modifier
                                    .clickable { viewModel.moveCursorRight() }
                                    .padding(8.dp), 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (viewModel.result.isNotEmpty()) {
                        Text(
                            text = viewModel.result,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Buttons Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val buttons = listOf(
                    listOf("C", "History", "Find X", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "−"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "Del", "=")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CalcButton("X", MaterialTheme.colorScheme.surfaceTint, MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)) { viewModel.append("X") }
                    Spacer(modifier = Modifier.width(8.dp))
                    CalcButton("Y", MaterialTheme.colorScheme.surfaceTint, MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)) { viewModel.append("Y") }
                    Spacer(modifier = Modifier.width(8.dp))
                    CalcButton("^2", MaterialTheme.colorScheme.surfaceTint, MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)) { viewModel.append("^2") }
                    Spacer(modifier = Modifier.width(8.dp))
                    CalcButton("√", MaterialTheme.colorScheme.surfaceTint, MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)) { viewModel.append("√(") }
                    Spacer(modifier = Modifier.width(8.dp))
                    CalcButton("³√", MaterialTheme.colorScheme.surfaceTint, MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)) { viewModel.append("³√(") }
                }

                for (row in buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (btn in row) {
                            val bgColor = when {
                                btn in listOf("÷", "×", "−", "+", "=") -> AccentColor
                                btn in listOf("C", "History", "Find X") -> Color(0xFFA5A5A5)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            }
                            val txtColor = when {
                                btn in listOf("÷", "×", "−", "+", "=") -> Color.White
                                btn in listOf("C", "History", "Find X") -> Color.Black
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            
                            CalcButton(
                                text = btn, 
                                backgroundColor = bgColor, 
                                textColor = txtColor, 
                                modifier = Modifier.weight(1f),
                                onLongClick = if (btn.toDoubleOrNull() != null) { 
                                    { viewModel.assignX(btn.toDouble()) } 
                                } else null
                            ) {
                                when (btn) {
                                    "C" -> viewModel.clear()
                                    "Del" -> viewModel.deleteAtCursor()
                                    "=" -> viewModel.calculate()
                                    "History" -> viewModel.toggleHistory()
                                    "Find X" -> viewModel.solveForX()
                                    else -> viewModel.append(btn)
                                }
                            }
                        }
                    }
                }
                
                // Variable assignment row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { viewModel.assignVariable("X") }) { Text("Set X = Result") }
                    Button(onClick = { viewModel.assignVariable("Y") }) { Text("Set Y = Result") }
                }
            }
        }

        // History Overlay
        if (viewModel.isHistoryVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.toggleHistory() },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f)
                        .clickable(enabled = false) {}, // absorb clicks
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calculation History", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            items(history) { item ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(item.expression, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    Text("= ${item.result}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalcButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f)

    Box(
        modifier = modifier
            .aspectRatio(if (text == "History" || text == "Find X") 2f else if (text in listOf("X","Y","^2","√","³√")) 1.5f else 1f)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource, 
                indication = null, 
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 2) 16.sp else 24.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
