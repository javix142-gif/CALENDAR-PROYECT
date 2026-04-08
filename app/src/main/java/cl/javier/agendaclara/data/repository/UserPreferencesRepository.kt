package cl.javier.agendaclara.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.javier.agendaclara.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("agenda_settings")

class UserPreferencesRepository(private val context: Context) {
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            reminder1 = prefs[KEY_REMINDER1] ?: 30,
            reminder2 = prefs[KEY_REMINDER2] ?: 10,
            defaultDurationMinutes = prefs[KEY_DURATION] ?: 60,
            highContrast = prefs[KEY_CONTRAST] ?: false,
            largeText = prefs[KEY_TEXT] ?: true,
            exactAlarmAtEventTime = true,
        )
    }

    suspend fun update(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMINDER1] = settings.reminder1
            prefs[KEY_REMINDER2] = settings.reminder2
            prefs[KEY_DURATION] = settings.defaultDurationMinutes
            prefs[KEY_CONTRAST] = settings.highContrast
            prefs[KEY_TEXT] = settings.largeText
        }
    }

    companion object {
        private val KEY_REMINDER1 = intPreferencesKey("reminder_1")
        private val KEY_REMINDER2 = intPreferencesKey("reminder_2")
        private val KEY_DURATION = intPreferencesKey("duration")
        private val KEY_CONTRAST = booleanPreferencesKey("contrast")
        private val KEY_TEXT = booleanPreferencesKey("large_text")
    }
}
