
package com.tripcraft.app.features.trips

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val destination: String,

    val startDate: Long,

    val endDate: Long,

    val coverColor: String = "#2196F3"
)
