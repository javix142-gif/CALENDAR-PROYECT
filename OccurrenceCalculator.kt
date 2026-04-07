package cl.javier.agendaclara

import android.app.Application
import cl.javier.agendaclara.core.AppContainer

class AgendaClaraApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.createChannels()
    }
}
