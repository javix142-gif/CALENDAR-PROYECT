package cl.javier.agendaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.javier.agendaclara.viewmodel.MainViewModel

@Composable
fun EditScreen(vm: MainViewModel, pad: PaddingValues, onSaved: () -> Unit) {
    val proposal = vm.proposal.value
    if (proposal == null) return
    var title by remember { mutableStateOf(proposal.title) }
    var notes by remember { mutableStateOf(proposal.notes ?: "") }
    var duration by remember { mutableIntStateOf(60) }
    var rem1 by remember { mutableIntStateOf(30) }
    var rem2 by remember { mutableIntStateOf(10) }
    Column(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(proposal.startDate.toString(), {}, enabled = false, label = { Text("Fecha") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(proposal.suggestedTime.toString(), {}, enabled = false, label = { Text("Hora") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(notes, { notes = it }, label = { Text("Detalle") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(duration.toString(), { duration = it.toIntOrNull() ?: duration }, label = { Text("Duración (min)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(rem1.toString(), { rem1 = it.toIntOrNull() ?: rem1 }, label = { Text("Recordatorio 1") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(rem2.toString(), { rem2 = it.toIntOrNull() ?: rem2 }, label = { Text("Recordatorio 2") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.saveFromProposal(proposal.copy(title = title), notes, duration, rem1, rem2); onSaved() }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
    }
}
