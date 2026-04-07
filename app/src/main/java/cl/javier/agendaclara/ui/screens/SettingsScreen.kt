package cl.javier.agendaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.javier.agendaclara.data.model.AppSettings
import cl.javier.agendaclara.viewmodel.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel, pad: PaddingValues) {
    val settings by vm.settings.collectAsState()
    var rem1 by remember(settings) { mutableIntStateOf(settings.reminder1) }
    var rem2 by remember(settings) { mutableIntStateOf(settings.reminder2) }
    var dur by remember(settings) { mutableIntStateOf(settings.defaultDurationMinutes) }
    Column(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(rem1.toString(), { rem1 = it.toIntOrNull() ?: rem1 }, label = { Text("Recordatorio 1") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(rem2.toString(), { rem2 = it.toIntOrNull() ?: rem2 }, label = { Text("Recordatorio 2") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(dur.toString(), { dur = it.toIntOrNull() ?: dur }, label = { Text("Duración por defecto") }, modifier = Modifier.fillMaxWidth())
        Text("Alto contraste")
        Switch(checked = settings.highContrast, onCheckedChange = { vm.updateSettings(settings.copy(highContrast = it)) })
        Text("Texto grande")
        Switch(checked = settings.largeText, onCheckedChange = { vm.updateSettings(settings.copy(largeText = it)) })
        Button(onClick = { vm.updateSettings(AppSettings(rem1, rem2, dur, settings.highContrast, settings.largeText, true)) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar ajustes") }
    }
}
