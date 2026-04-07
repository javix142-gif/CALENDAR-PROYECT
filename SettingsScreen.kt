package cl.javier.agendaclara.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.javier.agendaclara.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "agenda_clara_settings")

class UserPreferencesRepository(private val context: Context) {

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            reminderOneMinutesBefore = prefs[Keys.REMINDER_ONE] ?: 30,
            reminderTwoMinutesBefore = prefs[Keys.REMINDER_TWO] ?: 10,
            defaultDurationMinutes = prefs[Keys.DEFAULT_DURATION] ?: 60,
            defaultSuggestedHour = prefs[Keys.DEFAULT_SUGGESTED_HOUR] ?: (9 * 60),
            highContrast = prefs[Keys.HIGH_CONTRAST] ?: true,
            exactAlarmAtEventTime = prefs[Keys.EXACT_ALARM_AT_EVENT] ?: true,
        )
    }

    suspend fun updateReminderMinutes(reminderOne: Int, reminderTwo: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMINDER_ONE] = reminderOne
            prefs[Keys.REMINDER_TWO] = reminderTwo
        }
    }

    suspend fun updateDefaults(durationMinutes: Int, suggestedHourMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_DURATION] = durationMinutes
            prefs[Keys.DEFAULT_SUGGESTED_HOUR] = suggestedHourMinutes
        }
    }

    suspend fun updateHighContrast(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIGH_CONTRAST] = enabled
        }
    }

    suspend fun updateExactAlarmAtEvent(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EXACT_ALARM_AT_EVENT] = enabled
        }
    }

    private object Keys {
        val REMINDER_ONE: Preferences.Key<Int> = intPreferencesKey("reminder_one")
        val REMINDER_TWO: Preferences.Key<Int> = intPreferencesKey("reminder_two")
        val DEFAULT_DURATION: Preferences.Key<Int> = intPreferencesKey("default_duration")
        val DEFAULT_SUGGESTED_HOUR: Preferences.Key<Int> = intPreferencesKey("default_suggested_hour")
        val HIGH_CONTRAST: Preferences.Key<Boolean> = booleanPreferencesKey("high_contrast")
        val EXACT_ALARM_AT_EVENT: Preferences.Key<Boolean> = booleanPreferencesKey("exact_alarm_at_event")
    }
}
