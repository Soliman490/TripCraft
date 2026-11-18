
package com.tripcraft.app.features.trips

import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    val allTrips: Flow<List<TripEntity>> = tripDao.getAllTrips()

    suspend fun insertTrip(trip: TripEntity) {
        tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: TripEntity) {
        tripDao.deleteTrip(trip)
    }

    suspend fun getTripById(tripId: Int): TripEntity? {
        return tripDao.getTripById(tripId)
    }
}
