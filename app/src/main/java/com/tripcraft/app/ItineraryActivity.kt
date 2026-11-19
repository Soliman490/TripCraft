package com.tripcraft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tripcraft.app.data.journal.TripCraftDatabase
import com.tripcraft.app.features.trips.TripEntity
import com.tripcraft.app.ui.theme.TripCraftTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF17243A)
private val Teal = Color(0xFF18A999)
private val CardSand = Color(0xFFFFF3E4)

class ItineraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tripId = intent.getIntExtra("ID", 0)

        setContent {
            TripCraftTheme {
                Scaffold { pad ->
                    ItineraryScreen(
                        tripId = tripId,
                        onBack = { finish() },
                        modifier = Modifier.padding(pad)
                    )
                }
            }
        }
    }
}

@Composable
fun ItineraryScreen(
    tripId: Int = 0,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeTripId by remember(tripId) { mutableIntStateOf(tripId) }
    var list by remember { mutableStateOf(listOf<ItineraryItem>()) }
    var day by rememberSaveable { mutableStateOf("Day 1") }
    var time by rememberSaveable { mutableStateOf("10:00 AM") }
    var title by rememberSaveable { mutableStateOf("") }

    fun refreshItinerary(action: (suspend (TripCraftDatabase, Int) -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            val db = TripCraftDatabase.getDatabase(context)
            var currentId = activeTripId
            if (currentId <= 0) {
                val existingTrips = db.tripDao().getAllTrips().firstOrNull() ?: emptyList()
                if (existingTrips.isNotEmpty()) {
                    currentId = existingTrips.first().id
                } else {
                    val now = System.currentTimeMillis()
                    val newTrip = TripEntity(
                        title = "My Trip",
                        destination = "General",
                        startDate = now,
                        endDate = now
                    )
                    currentId = db.tripDao().insertTrip(newTrip).toInt()
                }
                withContext(Dispatchers.Main) {
                    activeTripId = currentId
                }
            }

            if (currentId > 0) {
                action?.invoke(db, currentId)
                val updated = db.itineraryDao().getForTrip(currentId)
                withContext(Dispatchers.Main) {
                    list = updated
                }
            }
        }
    }

    LaunchedEffect(tripId) {
        refreshItinerary()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                text = "Itinerary (${list.count { it.done }}/${list.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Navy,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = day,
                onValueChange = { day = it },
                label = { Text("Day") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Activity") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        refreshItinerary { db, currentId ->
                            db.itineraryDao().insert(
                                ItineraryItem(
                                    tripId = currentId,
                                    day = day.trim(),
                                    title = title.trim(),
                                    time = time.trim()
                                )
                            )
                        }
                        title = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text("Add")
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSand),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            refreshItinerary { db, _ ->
                                db.itineraryDao().update(item.copy(done = !item.done))
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${item.day} ${item.time}] ${item.title}",
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (item.done) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                refreshItinerary { db, _ ->
                                    db.itineraryDao().delete(item)
                                }
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
