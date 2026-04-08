package cl.javier.agendaclara.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cl.javier.agendaclara.ui.components.AgendaItemCard
import cl.javier.agendaclara.viewmodel.MainViewModel

@Composable
fun TodayScreen(vm: MainViewModel, pad: PaddingValues, onEdit: () -> Unit) {
    val items by vm.today.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = pad) {
        items(items) { item -> AgendaItemCard(item, onDone = { vm.markDone(item.id, it) }, onEdit = onEdit) }
    }
}
