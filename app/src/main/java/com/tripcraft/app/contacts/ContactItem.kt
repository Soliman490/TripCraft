
package com.tripcraft.app.contacts

data class ContactItem(
    val id: Int,
    val name: String,
    val phone: String,
    val type: ContactType
)

enum class ContactType {
    EMERGENCY,
    HOTEL
}
