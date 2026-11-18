
package com.tripcraft.app.features.trips

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val TripNavy = Color(0xFF17243A)
private val TripTeal = Color(0xFF18A999)
private val TripBackground = Color(0xFFF5F7FA)

@Composable
fun TripHubScreen(
    viewModel: TripViewModel
) {
    val trips by viewModel.allTrips.collectAsState()
    val context = LocalContext.current

    var showAddTripDialog by remember {
        mutableStateOf(false)
    }

    var tripTitle by remember { mutableStateOf("") }
    var tripDestination by remember { mutableStateOf("") }

    var startDate by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var endDate by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    val currentTime = System.currentTimeMillis()

    val filteredTrips = trips.filter {
        it.title.contains(searchQuery.trim(), ignoreCase = true) ||
                it.destination.contains(searchQuery.trim(), ignoreCase = true)
    }

    val upcomingTrips = filteredTrips
        .filter { it.endDate >= currentTime }
        .sortedBy { it.startDate }

    val pastTrips = filteredTrips
        .filter { it.endDate < currentTime }
        .sortedByDescending { it.startDate }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TripBackground),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TripNavy)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 24.dp
                    )
            ) {
                Text(
                    text = "TripCraft",
                    color = TripTeal,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "My Trips",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Plan your adventures and keep every trip organized.",
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${trips.size} saved trips",
                    color = Color.White
                )
            }
        }

        // Add Trip Button
        item {
            Button(
                onClick = {
                    tripTitle = ""
                    tripDestination = ""
                    startDate = System.currentTimeMillis()
                    endDate = System.currentTimeMillis()
                    showAddTripDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TripTeal
                )
            ) {
                Text(
                    text = "+ Add Trip",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Search
        if (trips.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Search trips")
                    },
                    placeholder = {
                        Text("Search by trip name or destination")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    searchQuery = ""
                                }
                            ) {
                                Text(
                                    "Clear",
                                    color = TripTeal
                                )
                            }
                        }
                    }
                )
            }
        }

        // Empty State
        if (trips.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✈",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No trips yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TripNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add your first trip and start planning your adventure.",
                        color = Color.Gray
                    )
                }
            }
        } else if (filteredTrips.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔍",
                        style = MaterialTheme.typography.displayMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No matching trips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TripNavy
                    )

                    Text(
                        text = "Try another trip name or destination.",
                        color = Color.Gray
                    )
                }
            }
        } else {

            // Upcoming Trips
            if (upcomingTrips.isNotEmpty()) {
                item {
                    Text(
                        text = "Upcoming Trips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TripNavy
                    )
                }

                items(
                    upcomingTrips,
                    key = { it.id }
                ) { trip ->
                    TripCard(
                        trip = trip,
                        status = "Upcoming"
                    )
                }
            }

            // Past Trips
            if (pastTrips.isNotEmpty()) {
                item {
                    Text(
                        text = "Past Trips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TripNavy
                    )
                }

                items(
                    pastTrips,
                    key = { it.id }
                ) { trip ->
                    TripCard(
                        trip = trip,
                        status = "Completed"
                    )
                }
            }
        }
    }

    // Add Trip Dialog
    if (showAddTripDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTripDialog = false
            },
            title = {
                Text(
                    text = "Add New Trip",
                    color = TripNavy,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tripTitle,
                        onValueChange = { tripTitle = it },
                        label = {
                            Text("Trip Name")
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tripDestination,
                        onValueChange = { tripDestination = it },
                        label = {
                            Text("Destination")
                        },
                        singleLine = true
                    )

                    // Start Date
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = startDate

                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selectedCalendar =
                                        Calendar.getInstance()

                                    selectedCalendar.set(
                                        year, month, day, 0, 0, 0
                                    )

                                    selectedCalendar.set(
                                        Calendar.MILLISECOND,
                                        0
                                    )

                                    startDate =
                                        selectedCalendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Start Date: ${formatTripDate(startDate)}",
                            color = TripTeal
                        )
                    }

                    // End Date
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = endDate

                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selectedCalendar =
                                        Calendar.getInstance()

                                    selectedCalendar.set(
                                        year, month, day, 0, 0, 0
                                    )

                                    selectedCalendar.set(
                                        Calendar.MILLISECOND,
                                        0
                                    )

                                    endDate =
                                        selectedCalendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "End Date: ${formatTripDate(endDate)}",
                            color = TripTeal
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (endDate >= startDate) {
                            val newTrip = TripEntity(
                                title = tripTitle.trim(),
                                destination = tripDestination.trim(),
                                startDate = startDate,
                                endDate = endDate
                            )

                            viewModel.insertTrip(newTrip)

                            tripTitle = ""
                            tripDestination = ""
                            startDate = System.currentTimeMillis()
                            endDate = System.currentTimeMillis()

                            showAddTripDialog = false
                        }
                    },
                    enabled = tripTitle.isNotBlank() &&
                            tripDestination.isNotBlank() &&
                            endDate >= startDate
                ) {
                    Text(
                        "Save",
                        color = TripTeal
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTripDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Trip Card
@Composable
private fun TripCard(
    trip: TripEntity,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TripNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "📍 ${trip.destination}",
                color = TripTeal
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${formatTripDate(trip.startDate)} - ${
                    formatTripDate(trip.endDate)
                }",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (status == "Upcoming") {
                    Color(0xFFE5F7F4)
                } else {
                    Color(0xFFECEFF3)
                }
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = TripNavy
                )
            }
        }
    }
}

private fun formatTripDate(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}
