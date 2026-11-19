package com.tripcraft.app.data.journal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tripcraft.app.ItineraryDao
import com.tripcraft.app.ItineraryItem
import com.tripcraft.app.PackingDao
import com.tripcraft.app.PackingItem
import com.tripcraft.app.data.expense.ExpenseDao
import com.tripcraft.app.data.expense.ExpenseEntity
import com.tripcraft.app.features.trips.TripDao
import com.tripcraft.app.features.trips.TripEntity

@Database(
    entities = [
        JournalNoteEntity::class,
        TripEntity::class,
        PackingItem::class,
        ItineraryItem::class,
        ExpenseEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class TripCraftDatabase : RoomDatabase() {

    abstract fun journalNoteDao(): JournalNoteDao

    abstract fun tripDao(): TripDao

    abstract fun packingDao(): PackingDao

    abstract fun itineraryDao(): ItineraryDao

    abstract fun expenseDao(): ExpenseDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS packing_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        packed INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS itinerary_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        day TEXT NOT NULL,
                        time TEXT NOT NULL,
                        title TEXT NOT NULL,
                        done INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL,
                        date INTEGER NOT NULL
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
