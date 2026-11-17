
package com.tripcraft.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripcraft.app.data.journal.JournalNoteEntity
import com.tripcraft.app.data.journal.JournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    val allNotes: StateFlow<List<JournalNoteEntity>> =
        repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertNote(note: JournalNoteEntity) {
        viewModelScope.launch {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: JournalNoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: JournalNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    suspend fun getNoteById(noteId: Int): JournalNoteEntity? {
        return repository.getNoteById(noteId)
    }
}
