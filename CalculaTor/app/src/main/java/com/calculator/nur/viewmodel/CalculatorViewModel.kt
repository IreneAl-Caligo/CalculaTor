package com.calculator.nur.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.nur.data.AppDatabase
import com.calculator.nur.data.MathHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.abs

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).mathHistoryDao()

    val history = dao.getHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    var expression by mutableStateOf("")
        private set

    var result by mutableStateOf("")
        private set

    var cursorPosition by mutableStateOf(0)
        private set

    var varX by mutableStateOf<Double?>(null)
        private set
        
    var varY by mutableStateOf<Double?>(null)
        private set

    var isHistoryVisible by mutableStateOf(false)

    fun toggleHistory() {
        isHistoryVisible = !isHistoryVisible
    }

    fun clear() {
        expression = ""
        result = ""
        cursorPosition = 0
    }

    fun append(char: String) {
        val before = expression.substring(0, cursorPosition)
        val after = expression.substring(cursorPosition)
        expression = before + char + after
        cursorPosition += char.length
        evaluatePreview()
    }

    fun deleteAtCursor() {
        if (cursorPosition > 0) {
            val before = expression.substring(0, cursorPosition - 1)
            val after = expression.substring(cursorPosition)
            expression = before + after
            cursorPosition--
            evaluatePreview()
        }
    }
    
    fun moveCursorLeft() {
        if (cursorPosition > 0) cursorPosition--
    }
    
    fun moveCursorRight() {
        if (cursorPosition < expression.length) cursorPosition++
    }

    fun assignVariable(name: String) {
        val currentRes = result.toDoubleOrNull()
        if (currentRes != null) {
            if (name == "X") varX = currentRes
            if (name == "Y") varY = currentRes
        } else {
            val eval = evaluateExpression(expression)
            eval.toDoubleOrNull()?.let {
                if (name == "X") varX = it
                if (name == "Y") varY = it
            }
        }
    }

    fun assignX(num: Double) {
        varX = num
    }

    fun solveForX() {
        var x0 = 0.0
        var x1 = 1.0
        
        fun f(x: Double): Double? {
            val evalStr = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace("√", "sqrt")
            return try {
                val eBuilder = ExpressionBuilder(evalStr)
                eBuilder.variables("X", "Y")
                val cbrt = object : net.objecthunter.exp4j.function.Function("cbrt", 1) {
                    override fun apply(vararg args: Double): Double {
                        return Math.cbrt(args[0])
                    }
                }
                eBuilder.functions(cbrt)
                val e = eBuilder.build()
                e.setVariable("X", x)
                e.setVariable("Y", varY ?: 0.0)
                e.evaluate()
            } catch (ex: Exception) {
                null
            }
        }
        
        for (i in 0..100) {
            val y0 = f(x0)
            val y1 = f(x1)
            if (y0 == null || y1 == null || y1.isNaN() || y0.isNaN()) {
                result = "Solver Error"
                return
            }
            if (abs(y1) < 1e-7) {
                varX = x1
                result = "X = ${if (x1 == x1.toLong().toDouble()) x1.toLong().toString() else x1}"
                return
            }
            if (y1 - y0 == 0.0) {
                x1 += 0.1
                continue
            }
            val x2 = x1 - y1 * (x1 - x0) / (y1 - y0)
            x0 = x1
            x1 = x2
        }
        result = "Solver Failed"
    }

    fun calculate() {
        val finalResult = evaluateExpression(expression)
        if (finalResult != "Error" && expression.isNotEmpty()) {
            viewModelScope.launch {
                dao.insertHistory(MathHistory(expression = expression, result = finalResult))
                dao.enforceLimit()
            }
            expression = finalResult
            result = ""
            cursorPosition = expression.length
        } else {
            result = "Error"
        }
    }

    private fun evaluatePreview() {
        if (expression.isEmpty()) {
            result = ""
            return
        }
        val res = evaluateExpression(expression)
        if (res != "Error") {
            result = res
        } else {
            result = ""
        }
    }

    private fun evaluateExpression(expr: String): String {
        return try {
            var evalStr = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace("√", "sqrt")
                .replace("³√", "cbrt")

            val eBuilder = ExpressionBuilder(evalStr)
            eBuilder.variables("X", "Y")
            
            val cbrt = object : net.objecthunter.exp4j.function.Function("cbrt", 1) {
                override fun apply(vararg args: Double): Double {
                    return Math.cbrt(args[0])
                }
            }
            eBuilder.functions(cbrt)
                
            val e = eBuilder.build()
            e.setVariable("X", varX ?: 0.0)
            e.setVariable("Y", varY ?: 0.0)
            
            val res = e.evaluate()
            if (res.isNaN() || res.isInfinite()) return "Error"
            
            if (res == res.toLong().toDouble()) {
                res.toLong().toString()
            } else {
                res.toString()
            }
        } catch (e: Exception) {
            "Error"
        }
    }
}
