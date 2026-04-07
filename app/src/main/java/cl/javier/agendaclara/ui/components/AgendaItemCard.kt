package cl.javier.agendaclara.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.javier.agendaclara.data.model.AgendaItem

@Composable
fun AgendaItemCard(item: AgendaItem, onDone: (Boolean) -> Unit, onEdit: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title)
            Text("${item.startDate} ${item.startTime ?: "(hora sugerida)"}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDone(!item.completed) }) { Text(if (item.completed) "Desmarcar" else "Realizado") }
                Button(onClick = onEdit) { Text("Editar") }
            }
        }
    }
}
