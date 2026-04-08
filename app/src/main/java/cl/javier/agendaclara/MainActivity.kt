package cl.javier.agendaclara

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import cl.javier.agendaclara.ui.navigation.AgendaApp
import cl.javier.agendaclara.ui.theme.AgendaTheme
import cl.javier.agendaclara.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels { MainViewModel.Factory((application as AgendaClaraApp).container) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            AgendaTheme(vm.uiSettings.value) { AgendaApp(vm) }
        }
    }
}
