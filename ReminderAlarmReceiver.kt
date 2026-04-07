package cl.javier.agendaclara.core

import android.content.Context
import androidx.room.Room
import cl.javier.agendaclara.data.local.AgendaDatabase
import cl.javier.agendaclara.data.repository.AgendaRepository
import cl.javier.agendaclara.data.repository.UserPreferencesRepository
import cl.javier.agendaclara.domain.NotificationHelper
import cl.javier.agendaclara.domain.ReminderScheduler
import cl.javier.agendaclara.domain.TextProposalParser

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AgendaDatabase by lazy {
        Room.databaseBuilder(appContext, AgendaDatabase::class.java, "agenda_clara.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val parser: TextProposalParser by lazy {
        TextProposalParser()
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(appContext)
    }

    val scheduler: ReminderScheduler by lazy {
        ReminderScheduler(
            context = appContext,
            preferencesRepository = preferencesRepository,
            agendaDao = database.agendaDao(),
            occurrenceStateDao = database.occurrenceStateDao(),
            notificationHelper = notificationHelper
        )
    }

    val repository: AgendaRepository by lazy {
        AgendaRepository(
            agendaDao = database.agendaDao(),
            occurrenceStateDao = database.occurrenceStateDao()
        )
    }
}
