package cl.javier.agendaclara.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.javier.agendaclara.ui.screens.EditScreen
import cl.javier.agendaclara.ui.screens.QuickAddScreen
import cl.javier.agendaclara.ui.screens.SettingsScreen
import cl.javier.agendaclara.ui.screens.TodayScreen
import cl.javier.agendaclara.ui.screens.UpcomingScreen
import cl.javier.agendaclara.viewmodel.MainViewModel

@Composable
fun AgendaApp(vm: MainViewModel) {
    val nav = rememberNavController()
    val current by nav.currentBackStackEntryAsState()
    val tabs = listOf("hoy", "proximos", "agregar", "ajustes")
    Scaffold(bottomBar = {
        NavigationBar { tabs.forEach { t -> NavigationBarItem(selected = current?.destination?.route == t, onClick = { nav.navigate(t) }, label = { Text(t.replaceFirstChar { it.uppercase() }) }, icon = {}) } }
    }) { pad ->
        NavHost(navController = nav, startDestination = "hoy", modifier = Modifier) {
            composable("hoy") { TodayScreen(vm, pad, onEdit = { nav.navigate("editar") }) }
            composable("proximos") { UpcomingScreen(vm, pad) }
            composable("agregar") { QuickAddScreen(vm, pad, onNext = { nav.navigate("editar") }) }
            composable("editar") { EditScreen(vm, pad, onSaved = { nav.navigate("hoy") }) }
            composable("ajustes") { SettingsScreen(vm, pad) }
        }
    }
}
