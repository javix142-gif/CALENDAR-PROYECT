package cl.javier.agendaclara

import android.app.Application
import androidx.room.Room
import cl.javier.agendaclara.data.local.AgendaDatabase
import cl.javier.agendaclara.data.repository.AgendaRepository
import cl.javier.agendaclara.data.repository.UserPreferencesRepository
import cl.javier.agendaclara.domain.ReminderScheduler
import cl.javier.agendaclara.domain.TextProposalParser
import cl.javier.agendaclara.notifications.NotificationHelper

class AgendaClaraApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, AgendaDatabase::class.java, "agenda-db").build()
        container = AppContainer(
            repository = AgendaRepository(db.agendaDao()),
            preferences = UserPreferencesRepository(this),
            parser = TextProposalParser(),
            scheduler = ReminderScheduler(this),
            notifications = NotificationHelper(this),
        )
        container.notifications.createChannels()
    }
}

data class AppContainer(
    val repository: AgendaRepository,
    val preferences: UserPreferencesRepository,
    val parser: TextProposalParser,
    val scheduler: ReminderScheduler,
    val notifications: NotificationHelper,
)
