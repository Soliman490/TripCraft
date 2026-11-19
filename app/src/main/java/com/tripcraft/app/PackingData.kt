package com.tripcraft.app

import androidx.room.*

@Entity(tableName = "packing_items")
data class PackingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val name: String,
    val category: String,
    val packed: Boolean = false
)

@Dao
interface PackingDao {
    @Insert
    suspend fun insert(item: PackingItem)

    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY id DESC")
    suspend fun getForTrip(tripId: Int): List<PackingItem>

    @Update
    suspend fun update(item: PackingItem)

    @Delete
    suspend fun delete(item: PackingItem)
}