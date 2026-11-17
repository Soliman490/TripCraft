
package com.tripcraft.app.data.journal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [JournalNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalNoteDao(): JournalNoteDao

    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "tripcraft_journal_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
