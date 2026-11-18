package com.example.tripcraft.data

import androidx.room.*

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE tripId = :tripId")
    suspend fun getExpensesForTrip(tripId: Int): List<Expense>

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
