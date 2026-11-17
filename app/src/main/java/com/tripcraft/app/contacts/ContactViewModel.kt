
package com.tripcraft.app.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = ContactStorage(application)

    private val _contacts = androidx.compose.runtime.mutableStateListOf<ContactItem>()

    val contacts: List<ContactItem>
        get() = _contacts

    init {
        val savedContacts = storage.loadContacts()

        if (savedContacts.isNotEmpty()) {
            _contacts.addAll(savedContacts)
        } else {
            _contacts.addAll(
                listOf(
                    ContactItem(1, "Emergency Services", "123", ContactType.EMERGENCY),
                    ContactItem(2, "Police", "122", ContactType.EMERGENCY),
                    ContactItem(3, "Ambulance", "123", ContactType.EMERGENCY)
                )
            )
            save()
        }
    }

    fun addContact(name: String, phone: String, type: ContactType) {
        if (name.isBlank() || phone.isBlank()) return

        _contacts.add(
            ContactItem(
                id = (_contacts.maxOfOrNull { it.id } ?: 0) + 1,
                name = name.trim(),
                phone = phone.trim(),
                type = type
            )
        )
        save()
    }

    fun updateContact(contact: ContactItem) {
        val index = _contacts.indexOfFirst { it.id == contact.id }

        if (index != -1) {
            _contacts[index] = contact
            save()
        }
    }

    fun deleteContact(contact: ContactItem) {
        _contacts.removeAll { it.id == contact.id }
        save()
    }

    private fun save() {
        storage.saveContacts(_contacts.toList())
    }
}
