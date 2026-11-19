package com.tripcraft.app.features.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tripcraft.app.data.expense.ExpenseEntity
import com.tripcraft.app.data.journal.TripCraftDatabase
import com.tripcraft.app.data.settings.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF17243A)
private val Teal = Color(0xFF18A999)
private val Coral = Color(0xFFFDE8E6)
private val Sage = Color(0xFFE5F4EA)
private val WarningRed = Color(0xFFE53935)
private val CardSand = Color(0xFFFFF3E4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    tripId: Int = 1,
    tripBudget: Double = 1000.0,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val currencySymbol by prefsRepo.currencyFlow.collectAsState(initial = "EGP")

    var expenses by remember { mutableStateOf(listOf<ExpenseEntity>()) }
    var showSheet by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }

    val categories = listOf("Food", "Transport", "Accommodation", "Entertainment", "Shopping", "Other")

    fun refreshExpenses() {
        scope.launch(Dispatchers.IO) {
            val db = TripCraftDatabase.getDatabase(context)
            val dao = db.expenseDao()
            dao.getExpensesForTrip(tripId).collect { items ->
                withContext(Dispatchers.Main) {
                    expenses = items
                }
            }
        }
    }

    LaunchedEffect(tripId) {
        refreshExpenses()
    }

    val totalSpent = expenses.sumOf { it.amount }

    val categoryTotals = expenses.fold(mapOf<String, Double>()) { acc, expense ->
        val currentAmount = acc[expense.category] ?: 0.0
        acc + (expense.category to (currentAmount + expense.amount))
    }

    val remainingBudget = (tripBudget - totalSpent).coerceAtLeast(0.0)
    val budgetUsageRatio = if (tripBudget > 0) totalSpent / tripBudget else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "Budget & Expenses",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Navy,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editingExpense = null
                    title = ""
                    amountText = ""
                    category = "Food"
                    showSheet = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text("+ Add Expense")
            }
        }

        if (budgetUsageRatio >= 0.8) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Coral),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = if (budgetUsageRatio >= 1.0) "⚠️ Budget Exceeded!" else "⚠️ Budget Alert: Over 80% Used",
                        fontWeight = FontWeight.Bold,
                        color = WarningRed
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Total Spent: $currencySymbol$totalSpent of $currencySymbol$tripBudget (Remaining: $currencySymbol${tripBudget - totalSpent})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Navy
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Sage),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("$currencySymbol$totalSpent", fontWeight = FontWeight.Bold, color = Navy, style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Remaining", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("$currencySymbol$remainingBudget", fontWeight = FontWeight.Bold, color = Teal, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        if (categoryTotals.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category Breakdown", fontWeight = FontWeight.Bold, color = Navy)
                    categoryTotals.forEach { (cat, amount) ->
                        val percent = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium, color = Navy)
                                Text("$currencySymbol$amount (${(percent * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Teal,
                                trackColor = Color(0xFFE0E0E0),
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(expenses, key = { it.id }) { expense ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSand),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editingExpense = expense
                            title = expense.title
                            amountText = expense.amount.toString()
                            category = expense.category
                            showSheet = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.title, fontWeight = FontWeight.Bold, color = Navy)
                            Text(
                                "${expense.category} • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "$currencySymbol${expense.amount}",
                            fontWeight = FontWeight.Bold,
                            color = Teal,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                val db = TripCraftDatabase.getDatabase(context)
                                db.expenseDao().deleteExpense(expense)
                            }
                        }) {
                            Text("🗑", color = WarningRed)
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (editingExpense == null) "Add Expense" else "Edit Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", fontWeight = FontWeight.Bold, color = Navy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Button(
                    onClick = {
                        val amountVal = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amountVal > 0) {
                            scope.launch(Dispatchers.IO) {
                                val db = TripCraftDatabase.getDatabase(context)
                                val item = ExpenseEntity(
                                    id = editingExpense?.id ?: 0,
                                    tripId = tripId,
                                    title = title.trim(),
                                    amount = amountVal,
                                    category = category
                                )
                                if (editingExpense == null) {
                                    db.expenseDao().insertExpense(item)
                                } else {
                                    db.expenseDao().updateExpense(item)
                                }
                                withContext(Dispatchers.Main) {
                                    showSheet = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text(if (editingExpense == null) "Save Expense" else "Update Expense")
                }
            }
        }
    }
}
