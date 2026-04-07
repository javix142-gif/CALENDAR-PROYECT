package cl.javier.agendaclara.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cl.javier.agendaclara.AgendaClaraApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as AgendaClaraApp
        CoroutineScope(Dispatchers.IO).launch {
            val items = app.container.repository.observeUpcoming(LocalDate.now(), LocalDate.now().plusYears(1)).first()
            app.container.scheduler.rescheduleAll(items)
            pending.finish()
        }
    }
}
