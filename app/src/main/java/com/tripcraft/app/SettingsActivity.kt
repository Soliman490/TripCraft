package com.tripcraft.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripcraft.app.data.journal.TripCraftDatabase
import com.tripcraft.app.data.settings.UserPreferencesRepository
import com.tripcraft.app.features.export.TripExporter
import com.tripcraft.app.ui.theme.TripCraftTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF17243A)
private val Teal = Color(0xFF18A999)

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TripCraftTheme {
                Scaffold { pad ->
                    SettingsScreen(
                        onBack = { finish() },
                        modifier = Modifier.padding(pad)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }

    val currency by prefsRepo.currencyFlow.collectAsState(initial = "EGP")
    val darkMode by prefsRepo.darkModeFlow.collectAsState(initial = "System")
    val sortOrder by prefsRepo.sortOrderFlow.collectAsState(initial = "Newest First")

    val currencyOptions = listOf("EGP", "$", "€", "£", "¥")
    val darkModeOptions = listOf("System", "Light", "Dark")
    val sortOrderOptions = listOf("Newest First", "Title A-Z", "Highest Rating")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                text = "Settings & Export",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Navy,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Currency Symbol", fontWeight = FontWeight.Bold, color = Navy, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencyOptions.forEach { symbol ->
                        FilterChip(
                            selected = currency == symbol,
                            onClick = {
                                scope.launch {
                                    prefsRepo.updateCurrency(symbol)
                                    Toast.makeText(context, "Currency set to $symbol", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text(symbol) }
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Theme Preference", fontWeight = FontWeight.Bold, color = Navy, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    darkModeOptions.forEach { mode ->
                        FilterChip(
                            selected = darkMode == mode,
                            onClick = {
                                scope.launch {
                                    prefsRepo.updateDarkMode(mode)
                                    Toast.makeText(context, "Theme set to $mode", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text(mode) }
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Default Sort Order", fontWeight = FontWeight.Bold, color = Navy, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOrderOptions.forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = {
                                scope.launch {
                                    prefsRepo.updateSortOrder(order)
                                    Toast.makeText(context, "Sort order set to $order", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text(order) }
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Export Full Trip Summary", fontWeight = FontWeight.Bold, color = Navy, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Generates a full summary report combining your itinerary, expenses, packing list, and journal notes, and shares it via Android Share Intent.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val db = TripCraftDatabase.getDatabase(context)
                            val trips = db.tripDao().getAllTrips().first()
                            val activeTrip = trips.firstOrNull()
                            if (activeTrip == null) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "No trip found to export", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }

                            val itinerary = db.itineraryDao().getForTrip(activeTrip.id)
                            val expenses = db.expenseDao().getExpensesForTrip(activeTrip.id).first()
                            val packing = db.packingDao().getForTrip(activeTrip.id)
                            val notes = db.journalNoteDao().getAllNotes().first()

                            withContext(Dispatchers.Main) {
                                TripExporter.exportTripReport(
                                    context = context,
                                    trip = activeTrip,
                                    itineraryList = itinerary,
                                    expenseList = expenses,
                                    packingList = packing,
                                    journalNotes = notes,
                                    currencySymbol = currency
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("📤 Export & Share Trip Report")
                }
            }
        }
    }
}
