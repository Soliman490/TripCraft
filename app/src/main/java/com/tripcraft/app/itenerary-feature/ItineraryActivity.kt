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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItineraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tripId = intent.getIntExtra("ID", 0)

        setContent {
            TripCraftTheme {
                Scaffold(containerColor = TripCraftColors.MistBackground) { pad ->
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

    fun refreshItinerary(action: (suspend (TripDatabase, Int) -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            val db = TripDatabase.get(context)
            var currentId = activeTripId
            if (currentId <= 0) {
                val existingTrips = db.tripDao().getAll()
                if (existingTrips.isNotEmpty()) {
                    currentId = existingTrips.first().id
                } else {
                    db.tripDao().insert(Trip(title = "My Trip", destination = "General", date = "Today", budget = 0.0))
                    currentId = db.tripDao().getAll().firstOrNull()?.id ?: 0
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
        HeaderBar(
            title = "Itinerary (${list.count { it.done }}/${list.size})",
            onBack = onBack
        )
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
                }
            ) {
                Text("Add")
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TripCraftColors.CardSand),
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
