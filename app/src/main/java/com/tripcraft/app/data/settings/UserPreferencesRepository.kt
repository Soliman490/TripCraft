package com.tripcraft.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val CURRENCY_KEY = stringPreferencesKey("currency_symbol")
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
    }

    val currencyFlow: Flow<String> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[CURRENCY_KEY] ?: "EGP"
    }

    val darkModeFlow: Flow<String> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: "System"
    }

    val sortOrderFlow: Flow<String> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[SORT_ORDER_KEY] ?: "Newest First"
    }

    suspend fun updateCurrency(currency: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[CURRENCY_KEY] = currency
        }
    }

    suspend fun updateDarkMode(mode: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = mode
        }
    }

    suspend fun updateSortOrder(order: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[SORT_ORDER_KEY] = order
        }
    }
}
