package cl.javier.agendaclara

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.javier.agendaclara.ui.AgendaClaraRoot
import cl.javier.agendaclara.ui.MainViewModel
import cl.javier.agendaclara.ui.MainViewModelFactory
import cl.javier.agendaclara.ui.theme.AgendaClaraTheme

class MainActivity : ComponentActivity() {

    private val notificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationsIfNeeded()

        setContent {
            val app = application as AgendaClaraApplication
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(app.container))
            AgendaClaraTheme {
                AgendaClaraRoot(viewModel = viewModel)
            }
        }
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
