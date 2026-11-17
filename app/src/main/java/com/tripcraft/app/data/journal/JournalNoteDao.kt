
package com.tripcraft.app.data.journal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalNoteDao {

    @Insert
    suspend fun insertNote(note: JournalNoteEntity): Long

    @Update
    suspend fun updateNote(note: JournalNoteEntity)

    @Delete
    suspend fun deleteNote(note: JournalNoteEntity)

    @Query("SELECT * FROM journal_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<JournalNoteEntity>>

    @Query("SELECT * FROM journal_notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): JournalNoteEntity?
}
