
package com.tripcraft.app.contacts

import android.content.Intent
import android.net.Uri
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

private val ContactNavy = Color(0xFF17243A)
private val ContactTeal = Color(0xFF18A999)
private val ContactBackground = Color(0xFFF5F7FA)

@Composable
fun ContactScreen(
    viewModel: ContactViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val contacts = viewModel.contacts

    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<ContactItem?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ContactType.EMERGENCY) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery.trim(), ignoreCase = true) ||
                it.phone.contains(searchQuery.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ContactBackground)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                        .background(ContactNavy)
                        .padding(
                            horizontal = 24.dp,
                            vertical = 24.dp
                        )
                ) {
                    Text(
                        "TripCraft",
                        color = ContactTeal,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Travel Contacts",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Keep important contacts close during your trip.",
                        color = Color.LightGray
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "${contacts.size} saved contacts",
                        color = Color.White
                    )
                }
            }

            // Add Contact Button
            item {
                Button(
                    onClick = {
                        editingContact = null
                        name = ""
                        phone = ""
                        type = ContactType.EMERGENCY
                        showDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ContactTeal
                    )
                ) {
                    Text(
                        "+ Add Contact",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search
            if (contacts.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search contacts") },
                        placeholder = {
                            Text("Search by name or phone")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                TextButton(
                                    onClick = { searchQuery = "" }
                                ) {
                                    Text("Clear", color = ContactTeal)
                                }
                            }
                        }
                    )
                }
            }

            // Empty State
            if (contacts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "☎",
                            style = MaterialTheme.typography.displayLarge
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "No contacts yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ContactNavy
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Add your first important travel contact.",
                            color = Color.Gray
                        )
                    }
                }
            } else if (filteredContacts.isEmpty()) {
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
                            "No matching contacts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ContactNavy
                        )

                        Text(
                            "Try another name or phone number.",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "${filteredContacts.size} contacts found",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                items(
                    filteredContacts,
                    key = { it.id }
                ) { contact ->

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
                                contact.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ContactNavy
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                "☎ ${contact.phone}",
                                color = ContactTeal
                            )

                            Spacer(Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (contact.type == ContactType.HOTEL) {
                                    Color(0xFFE8F4FF)
                                } else {
                                    Color(0xFFFFF0F0)
                                }
                            ) {
                                Text(
                                    if (contact.type == ContactType.HOTEL) {
                                        "Hotel"
                                    } else {
                                        "Emergency"
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = ContactNavy
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse(
                                                "tel:${Uri.encode(contact.phone)}"
                                            )
                                        )
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Text("Call", color = ContactTeal)
                                }

                                TextButton(
                                    onClick = {
                                        editingContact = contact
                                        name = contact.name
                                        phone = contact.phone
                                        type = contact.type
                                        showDialog = true
                                    }
                                ) {
                                    Text("Edit", color = ContactTeal)
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.deleteContact(contact)
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
    }

    // Add / Edit Contact Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(
                    if (editingContact == null) {
                        "Add Contact"
                    } else {
                        "Edit Contact"
                    },
                    color = ContactNavy,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact name") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone number") },
                        singleLine = true
                    )

                    Text(
                        "Contact type",
                        fontWeight = FontWeight.Bold,
                        color = ContactNavy
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == ContactType.EMERGENCY,
                            onClick = {
                                type = ContactType.EMERGENCY
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ContactTeal
                            )
                        )

                        Text("Emergency")

                        Spacer(Modifier.width(8.dp))

                        RadioButton(
                            selected = type == ContactType.HOTEL,
                            onClick = {
                                type = ContactType.HOTEL
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ContactTeal
                            )
                        )

                        Text("Hotel")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank() && phone.isNotBlank(),
                    onClick = {
                        val old = editingContact

                        if (old == null) {
                            viewModel.addContact(
                                name.trim(),
                                phone.trim(),
                                type
                            )
                        } else {
                            viewModel.updateContact(
                                old.copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    type = type
                                )
                            )
                        }

                        showDialog = false
                    }
                ) {
                    Text("Save", color = ContactTeal)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
