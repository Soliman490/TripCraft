
package com.tripcraft.app.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Emergency & Hotel Contacts",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                editingContact = null
                name = ""
                phone = ""
                type = ContactType.EMERGENCY
                showDialog = true
            }
        ) {
            Text("Add Contact")
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(contacts, key = { it.id }) { contact ->
                Card(
                    Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            contact.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(contact.phone)

                        Text(
                            if (contact.type == ContactType.HOTEL) {
                                "Hotel"
                            } else {
                                "Emergency"
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:${Uri.encode(contact.phone)}")
                                    )
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Call")
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
                                Text("Edit")
                            }

                            TextButton(
                                onClick = {
                                    viewModel.deleteContact(contact)
                                }
                            ) {
                                Text(
                                    "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

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
                    }
                ),
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

                    Text("Contact type")

                    Row {
                        RadioButton(
                            selected = type == ContactType.EMERGENCY,
                            onClick = {
                                type = ContactType.EMERGENCY
                            }
                        )
                        Text(
                            "Emergency",
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        RadioButton(
                            selected = type == ContactType.HOTEL,
                            onClick = {
                                type = ContactType.HOTEL
                            }
                        )
                        Text(
                            "Hotel",
                            modifier = Modifier.padding(top = 12.dp)
                        )
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
                    Text("Save")
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
