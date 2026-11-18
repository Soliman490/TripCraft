
package com.tripcraft.app.data.journal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tripcraft.app.features.trips.TripDao
import com.tripcraft.app.features.trips.TripEntity

@Database(
    entities = [
        JournalNoteEntity::class,
        TripEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TripCraftDatabase : RoomDatabase() {

    abstract fun journalNoteDao(): JournalNoteDao

    abstract fun tripDao(): TripDao

    companion object {

        @Volatile
        private var INSTANCE: TripCraftDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS trips (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        destination TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        coverColor TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): TripCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TripCraftDatabase::class.java,
                    "tripcraft_journal_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
