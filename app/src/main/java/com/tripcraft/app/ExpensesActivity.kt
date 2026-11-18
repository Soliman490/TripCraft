package com.example.tripcraft

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.tripcraft.data.Expense
import com.example.tripcraft.data.TripDatabase
import com.example.tripcraft.ui.theme.TripCraftTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Design System Colors
val NavyColor = Color(0xFF17243A)
val TealColor = Color(0xFF18A999)
val BackgroundColor = Color(0xFFF5F7FA)

class ExpensesActivity : ComponentActivity() {

    private var expensesState by mutableStateOf(listOf<Expense>())
    private var tripId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tripId = intent.getIntExtra("EXTRA_TRIP_ID", 0)
        val totalBudget = intent.getDoubleExtra("EXTRA_TRIP_BUDGET", 0.0)
        val sharedPref = getSharedPreferences("TripCraftPrefs", Context.MODE_PRIVATE)
        val currency = sharedPref.getString("currency", "EGP") ?: "EGP"

        loadExpenses()

        setContent {
            TripCraftTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExpensesScreen(
                        currency = currency,
                        totalBudget = totalBudget,
                        expenses = expensesState,
                        onAddExpense = { title, category, amount -> addExpense(title, category, amount) },
                        onDeleteExpense = { expense -> deleteExpense(expense) },
                        onBack = { finish() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun loadExpenses() {
        val db = TripDatabase.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.expenseDao().getExpensesForTrip(tripId)
            withContext(Dispatchers.Main) {
                expensesState = list
            }
        }
    }

    private fun addExpense(title: String, category: String, amount: Double) {
        val db = TripDatabase.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.expenseDao().insertExpense(
                Expense(tripId = tripId, title = title, category = category, amount = amount)
            )
            val updated = db.expenseDao().getExpensesForTrip(tripId)
            withContext(Dispatchers.Main) {
                expensesState = updated
                Toast.makeText(this@ExpensesActivity, "Expense logged!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteExpense(expense: Expense) {
        val db = TripDatabase.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.expenseDao().deleteExpense(expense)
            val updated = db.expenseDao().getExpensesForTrip(tripId)
            withContext(Dispatchers.Main) {
                expensesState = updated
            }
        }
    }
}

@Composable
fun ExpensesScreen(
    currency: String,
    totalBudget: Double,
    expenses: List<Expense>,
    onAddExpense: (String, String, Double) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var titleInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Food") }
    var amountInput by remember { mutableStateOf("") }

    val totalSpent = expenses.sumOf { it.amount }
    val remaining = totalBudget - totalSpent
    val isOver80Percent = totalBudget > 0 && (totalSpent / totalBudget) >= 0.8

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyColor)
            ) { 
                Text("Back") 
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Budget & Expenses",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyColor 
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

       
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isOver80Percent) Color(0xFFFFEBEE) else Color.White)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    "Spent: $currency $totalSpent / $currency $totalBudget",
                    fontWeight = FontWeight.Bold,
                    color = NavyColor
                )
                Text("Remaining: $currency $remaining", color = Color.DarkGray)
                if (isOver80Percent) {
                    Text("Warning: Over 80% of budget used!", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text("Expense Item (e.g., Dinner)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealColor,
                focusedLabelColor = TealColor
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = categoryInput,
                onValueChange = { categoryInput = it },
                label = { Text("Category") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealColor,
                    focusedLabelColor = TealColor
                )
            )
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount ($currency)") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealColor,
                    focusedLabelColor = TealColor
                )
            )
            Button(
                onClick = {
                    val amount = amountInput.trim().toDoubleOrNull()
                    if (titleInput.isBlank() || amount == null || amount <= 0.0) {
                        Toast.makeText(context, "Enter valid title and amount", Toast.LENGTH_SHORT).show()
                    } else {
                        onAddExpense(titleInput.trim(), categoryInput.trim().ifEmpty { "General" }, amount)
                        titleInput = ""
                        amountInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealColor) 
            ) {
                Text("Add", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = expenses) { expense ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(expense.title, fontWeight = FontWeight.Bold, color = NavyColor)
                        Text("Category: ${expense.category}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("$currency ${expense.amount}", fontWeight = FontWeight.Bold, color = TealColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onDeleteExpense(expense) }) {
                        Text("Delete", color = Color.Red)
                    }
                }
            }
        }
    }
}
