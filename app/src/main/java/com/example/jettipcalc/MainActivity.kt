package com.example.jettipcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jettipcalc.components.InputField
import com.example.jettipcalc.ui.theme.JetTipCalcTheme
import com.example.jettipcalc.widgets.RoundIconButton
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp {
                val systemUiController = rememberSystemUiController()
                val isDarkTheme = isSystemInDarkTheme()
                systemUiController.setStatusBarColor(
                    color = MaterialTheme.colorScheme.background,
                    darkIcons = !isDarkTheme
                )
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    JetTipCalcTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                )
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun MainContent() {
    Column {
        BillFormUi()
    }
}

@Composable
fun ResultCardUi(totalPerPerson: Double = 0.0) {
    val total: String = "%.2f".format(totalPerPerson)
    Surface(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
            .fillMaxWidth()
            .height(150.dp)
            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp))),
        color = Color(color = 0xFFE9D7F7)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Per Person",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )
            Row {
                Text(
                    text = "$",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                )
                Text(
                    text = total,
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                )
            }
        }
    }
}

@Composable
fun BillFormUi(onValChange: (String) -> Unit = {}) {
    val totalBillState = remember {
        mutableStateOf("")
    }
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val sliderPositionState = remember {
        mutableFloatStateOf(0f)
    }
    val splitByState = remember {
        mutableIntStateOf(1)
    }
    val tipAmountState = remember {
        mutableDoubleStateOf(0.0)
    }
    val totalPerPersonState = remember {
        mutableDoubleStateOf(0.0)
    }
    ResultCardUi(totalPerPersonState.doubleValue)
    Surface(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputField(
                valueState = totalBillState,
                labelId = "Enter Bill",
                enabled = true,
                isSingleLine = true,
                onAction = KeyboardActions {
                    if (!validState) return@KeyboardActions
                    onValChange(totalBillState.value.trim())
                    keyboardController?.hide()
                },
                onValueChange = {
                    if (it.isNotEmpty()) {
                        totalBillState.value = it
                        tipAmountState.doubleValue = calculateTotalTip(
                            totalBillState.value.toDouble(),
                            sliderPositionState.floatValue.toInt()
                        )
                        totalPerPersonState.doubleValue = totalPerPersonCalculation(
                            totalBillState.value.toDouble(),
                            splitByState.intValue,
                            sliderPositionState.floatValue.toInt()
                        )
                    } else {
                        totalBillState.value = ""
                        totalPerPersonState.doubleValue = 0.0
                    }
                }
            )
            if (validState) {
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically),
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Row {
                        RoundIconButton(
                            imageVector = Icons.Default.Remove,
                            size = 30.dp,
                            onClick = {
                                splitByState.intValue =
                                    if (splitByState.intValue > 1) splitByState.intValue - 1 else 1
                                totalPerPersonState.doubleValue = totalPerPersonCalculation(
                                    totalBillState.value.toDouble(),
                                    splitByState.intValue,
                                    sliderPositionState.floatValue.toInt()
                                )
                            }
                        )
                        Text(
                            text = "${splitByState.intValue}",
                            modifier = Modifier
                                .align(alignment = Alignment.CenterVertically)
                                .padding(start = 9.dp, end = 9.dp),
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        )
                        RoundIconButton(
                            imageVector = Icons.Default.Add,
                            size = 30.dp,
                            onClick = {
                                splitByState.intValue += 1
                                totalPerPersonState.doubleValue = totalPerPersonCalculation(
                                    totalBillState.value.toDouble(),
                                    splitByState.intValue,
                                    sliderPositionState.floatValue.toInt()
                                )
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tip",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically),
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Row {
                        Text(
                            text = "$",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val tip = "%.2f".format(tipAmountState.doubleValue)
                        Text(
                            text = tip,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        Text(
                            text = "${sliderPositionState.floatValue.toInt()}",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = " %",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Slider(
                        value = sliderPositionState.floatValue,
                        onValueChange = {
                            sliderPositionState.floatValue = it
                            tipAmountState.doubleValue = calculateTotalTip(
                                totalBillState.value.toDouble(),
                                sliderPositionState.floatValue.toInt()
                            )
                            totalPerPersonState.doubleValue = totalPerPersonCalculation(
                                totalBillState.value.toDouble(),
                                splitByState.intValue,
                                sliderPositionState.floatValue.toInt()
                            )
                        },
                        valueRange = 0f..100f,
                        steps = 100,
                        onValueChangeFinished = {

                        }
                    )
                }
            }
        }
    }
}

fun calculateTotalTip(totalBill: Double, tipPercentage: Int): Double {
    return if (totalBill > 0 && totalBill.toString()
            .isNotEmpty()
    ) (totalBill * tipPercentage) / 100 else 0.0
}

fun totalPerPersonCalculation(totalBill: Double, splitBy: Int, tipPercentage: Int): Double {
    return (calculateTotalTip(totalBill, tipPercentage) + totalBill) / splitBy
}