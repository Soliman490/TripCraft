
package com.tripcraft.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripcraft.app.contacts.ContactScreen
import com.tripcraft.app.data.journal.JournalDatabase
import com.tripcraft.app.data.journal.JournalNoteEntity
import com.tripcraft.app.data.journal.JournalRepository
import com.tripcraft.app.ui.theme.TripCraftTheme
import com.tripcraft.app.viewmodel.JournalViewModel

private val Navy = Color(0xFF17243A)
private val Teal = Color(0xFF18A999)
private val PageBackground = Color(0xFFF5F7FA)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = JournalDatabase.getDatabase(applicationContext)
        val repository = JournalRepository(database.journalNoteDao())

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return JournalViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            TripCraftTheme {
                val journalViewModel: JournalViewModel = viewModel(
                    factory = factory
                )

                TripCraftHome(journalViewModel)
            }
        }
    }
}

@Composable
fun TripCraftHome(viewModel: JournalViewModel) {

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("✈") },
                    label = { Text("Journal") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🗓") },
                    label = { Text("Itinerary") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("☎") },
                    label = { Text("Contacts") }
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> TravelJournalScreen(viewModel)
                1 -> ItineraryScreen()
                else -> ContactScreen()
            }
        }
    }
}

@Composable
fun TravelJournalScreen(viewModel: JournalViewModel) {

    val notes by viewModel.allNotes.collectAsState()

    var showDialog by rememberSaveable { mutableStateOf(false) }

    var selectedNote by rememberSaveable {
        mutableStateOf<JournalNoteEntity?>(null)
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Sorting states
    var sortOption by rememberSaveable { mutableStateOf("Newest First") }
    var sortMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Search and sort memories
    val filteredNotes = rememberSaveable(notes, searchQuery, sortOption) {

        val searchedNotes = notes.filter { note ->
            note.title.contains(searchQuery.trim(), ignoreCase = true) ||
                    note.location.contains(searchQuery.trim(), ignoreCase = true)
        }

        when (sortOption) {
            "Highest Rating" -> searchedNotes.sortedByDescending { it.rating }

            "Title A-Z" -> searchedNotes.sortedBy {
                it.title.lowercase()
            }

            else -> searchedNotes.sortedByDescending { it.id }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy)
                        .padding(
                            horizontal = 24.dp,
                            vertical = 24.dp
                        )
                ) {
                    Text(
                        "TripCraft",
                        color = Teal,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Travel Journal",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Keep your favorite travel memories.",
                        color = Color.LightGray
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "${notes.size} saved memories",
                        color = Color.White
                    )
                }
            }

            // Search Field
            if (notes.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search memories") },
                        placeholder = {
                            Text("Search by title or location")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                TextButton(
                                    onClick = { searchQuery = "" }
                                ) {
                                    Text("Clear", color = Teal)
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "${filteredNotes.size} results found",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Sort Memories
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Sort by",
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Box {
                            OutlinedButton(
                                onClick = {
                                    sortMenuExpanded = true
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(sortOption, color = Teal)
                                Text("  ▼", color = Teal)
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = {
                                    sortMenuExpanded = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Newest First") },
                                    onClick = {
                                        sortOption = "Newest First"
                                        sortMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Highest Rating") },
                                    onClick = {
                                        sortOption = "Highest Rating"
                                        sortMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Title A-Z") },
                                    onClick = {
                                        sortOption = "Title A-Z"
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Empty State
            if (notes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "✈",
                                style = MaterialTheme.typography.displayLarge
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "No memories yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Add your first travel memory!",
                                color = Color.Gray
                            )
                        }
                    }
                }

            } else if (filteredNotes.isEmpty()) {

                // No Search Results
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🔍",
                            style = MaterialTheme.typography.displayMedium
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "No matching memories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Try another title or location.",
                            color = Color.Gray
                        )
                    }
                }

            } else {

                // Memory Cards
                items(
                    filteredNotes,
                    key = { it.id }
                ) { note ->

                    MemoryCard(
                        note = note,
                        onEdit = {
                            selectedNote = note
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteNote(note)
                        }
                    )
                }
            }
        }

        // Fixed Add Memory Button
        FloatingActionButton(
            onClick = {
                selectedNote = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Teal,
            contentColor = Color.White
        ) {
            Text(
                "+",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }

    // Add / Edit Memory Dialog
    if (showDialog) {
        MemoryDialog(
            note = selectedNote,
            onDismiss = {
                showDialog = false
            },
            onSave = { title, description, location, rating, imageUri ->

                if (selectedNote == null) {
                    viewModel.insertNote(
                        JournalNoteEntity(
                            title = title,
                            description = description,
                            location = location,
                            rating = rating,
                            imageUri = imageUri
                        )
                    )
                } else {
                    viewModel.updateNote(
                        selectedNote!!.copy(
                            title = title,
                            description = description,
                            location = location,
                            rating = rating,
                            imageUri = imageUri
                        )
                    )
                }

                showDialog = false
            }
        )
    }
}

@Composable
fun MemoryCard(
    note: JournalNoteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
        Column(Modifier.padding(18.dp)) {

            Text(
                note.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "📍 ${note.location}",
                color = Teal
            )

            if (!note.imageUri.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))

                AsyncImage(
                    model = note.imageUri,
                    contentDescription = "Travel memory photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                note.description,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "★ ${note.rating}/5",
                color = Color(0xFFE6A23C),
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit", color = Teal)
                }

                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun MemoryDialog(
    note: JournalNoteEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Float, String?) -> Unit
) {
    val context = LocalContext.current

    var title by remember(note) {
        mutableStateOf(note?.title ?: "")
    }

    var description by remember(note) {
        mutableStateOf(note?.description ?: "")
    }

    var location by remember(note) {
        mutableStateOf(note?.location ?: "")
    }

    var rating by remember(note) {
        mutableStateOf(note?.rating?.toString() ?: "5")
    }

    var selectedImageUri by remember(note) {
        mutableStateOf(
            note?.imageUri?.let {
                Uri.parse(it)
            }
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Continue with temporary permission
            }

            selectedImageUri = uri
        }
    }

    val ratingValue = rating.toFloatOrNull()

    val validInput = title.isNotBlank() &&
            description.isNotBlank() &&
            location.isNotBlank() &&
            ratingValue != null &&
            ratingValue in 0f..5f

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                if (note == null) "Add Memory" else "Edit Memory"
            )
        },

        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Memory title") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3
                )

                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = { Text("Rating (0 - 5)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal
                    )
                ) {
                    Text(
                        if (selectedImageUri == null) {
                            "Choose Photo"
                        } else {
                            "Change Photo"
                        }
                    )
                }

                selectedImageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected travel photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        title.trim(),
                        description.trim(),
                        location.trim(),
                        ratingValue!!,
                        selectedImageUri?.toString()
                    )
                },
                enabled = validInput
            ) {
                Text("Save", color = Teal)
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
