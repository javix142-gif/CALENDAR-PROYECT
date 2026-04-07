package cl.javier.agendaclara.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cl.javier.agendaclara.AgendaClaraApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as AgendaClaraApplication
        CoroutineScope(Dispatchers.IO).launch {
            app.container.scheduler.rescheduleAll()
            pendingResult.finish()
        }
    }
}
