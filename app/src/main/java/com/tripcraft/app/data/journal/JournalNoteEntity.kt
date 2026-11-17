
package com.tripcraft.app.data.journal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_notes")
data class JournalNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val location: String,

    val rating: Float,

    val imageUri: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)
