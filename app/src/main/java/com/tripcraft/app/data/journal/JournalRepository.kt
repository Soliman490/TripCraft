
package com.tripcraft.app.data.journal

import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val journalNoteDao: JournalNoteDao
) {

    val allNotes: Flow<List<JournalNoteEntity>> =
        journalNoteDao.getAllNotes()

    suspend fun insertNote(note: JournalNoteEntity) {
        journalNoteDao.insertNote(note)
    }

    suspend fun updateNote(note: JournalNoteEntity) {
        journalNoteDao.updateNote(note)
    }

    suspend fun deleteNote(note: JournalNoteEntity) {
        journalNoteDao.deleteNote(note)
    }

    suspend fun getNoteById(noteId: Int): JournalNoteEntity? {
        return journalNoteDao.getNoteById(noteId)
    }
}
