package com.tripcraft.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "itinerary_items")
data class ItineraryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int = 0,
    val day: String = "Day 1",
    val time: String = "10:00 AM",
    val title: String = "",
    val done: Boolean = false
)

@Dao
interface ItineraryDao {
    @Insert
    suspend fun insert(item: ItineraryItem)

    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY id ASC")
    suspend fun getForTrip(tripId: Int): List<ItineraryItem>

    @Update
    suspend fun update(item: ItineraryItem)

    @Delete
    suspend fun delete(item: ItineraryItem)
}
