package com.tripcraft.app

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.room.*

@Dao
interface TripDao {
    @Insert suspend fun insert(trip: Trip)
    @Query("SELECT * FROM trips") suspend fun getAll(): List<Trip>
    @Query("SELECT * FROM trips WHERE id = :id") suspend fun getById(id: Int): Trip?
    @Update suspend fun update(trip: Trip)
    @Delete suspend fun delete(trip: Trip)
    @Transaction @Query("SELECT * FROM trips WHERE id = :id") suspend fun getDetails(id: Int): TripWithDetails?
}

@Dao
interface ItineraryDao {
    @Insert suspend fun insert(item: ItineraryItem)
    @Query("SELECT * FROM itinerary WHERE tripId = :tripId") suspend fun getForTrip(tripId: Int): List<ItineraryItem>
    @Update suspend fun update(item: ItineraryItem)
    @Delete suspend fun delete(item: ItineraryItem)
}

@Dao
interface ExpenseDao {
    @Insert suspend fun insert(expense: Expense)
    @Query("SELECT * FROM expenses WHERE tripId = :tripId") suspend fun getForTrip(tripId: Int): List<Expense>
    @Update suspend fun update(expense: Expense)
    @Delete suspend fun delete(expense: Expense)
}

@Dao
interface PackingDao {
    @Insert suspend fun insert(item: PackingItem)
    @Query("SELECT * FROM packing WHERE tripId = :tripId") suspend fun getForTrip(tripId: Int): List<PackingItem>
    @Update suspend fun update(item: PackingItem)
    @Delete suspend fun delete(item: PackingItem)
}

@Dao
interface JournalDao {
    @Insert suspend fun insert(entry: JournalEntry)
    @Query("SELECT * FROM journal WHERE tripId = :tripId") suspend fun getForTrip(tripId: Int): List<JournalEntry>
    @Update suspend fun update(entry: JournalEntry)
    @Delete suspend fun delete(entry: JournalEntry)
}

@Database(
    entities = [Trip::class, ItineraryItem::class, Expense::class, PackingItem::class, JournalEntry::class],
    version = 1
)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun packingDao(): PackingDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile private var INSTANCE: TripDatabase? = null
        fun get(context: Context): TripDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, TripDatabase::class.java, "tripcraft_db")
                .build().also { INSTANCE = it }
        }
    }
}

object TripCraftColors {
    val OasisTeal = Color(0xFF0F4C5C)
    val Terracotta = Color(0xFFE36414)
    val Saffron = Color(0xFFFB8B24)
    val MistBackground = Color(0xFFF6F9FA)
    val CardGlacier = Color(0xFFE2F1F5)
    val CardSand = Color(0xFFFFF3E4)
    val CardSage = Color(0xFFE5F4EA)
    val CardCoral = Color(0xFFFDE8E6)
    val CardLavender = Color(0xFFEDE8F8)
}

private val AppColorScheme = lightColorScheme(
    primary = TripCraftColors.OasisTeal,
    onPrimary = Color.White,
    secondary = TripCraftColors.Terracotta,
    tertiary = TripCraftColors.Saffron,
    background = TripCraftColors.MistBackground,
    surface = Color.White
)

@Composable
fun TripCraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, content = content)
}

@Composable
fun HeaderBar(title: String, onBack: (() -> Unit)? = null, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (onBack != null) {
            FilledTonalButton(onClick = onBack, shape = RoundedCornerShape(10.dp)) { Text("Back") }
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = TripCraftColors.OasisTeal,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}