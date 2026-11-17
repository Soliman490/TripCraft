
package com.tripcraft.app.contacts

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ContactStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "tripcraft_contacts",
        Context.MODE_PRIVATE
    )

    fun saveContacts(contacts: List<ContactItem>) {
        val array = JSONArray()

        contacts.forEach { contact ->
            array.put(
                JSONObject().apply {
                    put("id", contact.id)
                    put("name", contact.name)
                    put("phone", contact.phone)
                    put("type", contact.type.name)
                }
            )
        }

        preferences.edit()
            .putString("contacts", array.toString())
            .apply()
    }

    fun loadContacts(): List<ContactItem> {
        val json = preferences.getString("contacts", null)
            ?: return emptyList()

        return try {
            val array = JSONArray(json)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)

                ContactItem(
                    id = item.getInt("id"),
                    name = item.getString("name"),
                    phone = item.getString("phone"),
                    type = ContactType.valueOf(item.getString("type"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
