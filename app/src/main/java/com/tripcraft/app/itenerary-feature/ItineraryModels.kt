package com.tripcraft.app

import androidx.room.*


@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val destination: String,
    val date: String,
    val budget: Double
)

@Entity(
    tableName = "itinerary",
    foreignKeys = [ForeignKey(Trip::class, ["id"], ["tripId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tripId")]
)
data class ItineraryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val day: String,
    val title: String,
    val time: String,
    val done: Boolean = false
)

data class TripWithDetails(
    @Embedded val trip: Trip,
    @Relation(parentColumn = "id", entityColumn = "tripId") val activities: List<ItineraryItem>,

    )

