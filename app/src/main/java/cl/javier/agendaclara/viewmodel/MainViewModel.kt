package cl.javier.agendaclara.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.javier.agendaclara.AppContainer
import cl.javier.agendaclara.data.model.AgendaItem
import cl.javier.agendaclara.data.model.AppSettings
import cl.javier.agendaclara.data.model.ItemType
import cl.javier.agendaclara.data.model.ParsedProposal
import cl.javier.agendaclara.data.model.RepeatRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainViewModel(private val container: AppContainer) : ViewModel() {
    val today: StateFlow<List<AgendaItem>> = container.repository.observeToday(LocalDate.now()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val upcoming: StateFlow<List<AgendaItem>> = container.repository.observeUpcoming(LocalDate.now(), LocalDate.now().plusDays(7)).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings: StateFlow<AppSettings> = container.preferences.settingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _proposal = mutableStateOf<ParsedProposal?>(null)
    val proposal: State<ParsedProposal?> = _proposal
    val uiSettings = mutableStateOf(AppSettings())

    init {
        viewModelScope.launch { settings.collect { uiSettings.value = it } }
    }

    fun interpret(text: String) { _proposal.value = container.parser.parse(text, LocalDate.now()) }

    fun saveFromProposal(p: ParsedProposal, notes: String, duration: Int, reminder1: Int, reminder2: Int) {
        viewModelScope.launch {
            val item = AgendaItem(
                title = p.title,
                type = p.type,
                startDate = p.startDate,
                startTime = p.suggestedTime,
                durationMinutes = duration,
                notes = notes,
                location = p.location,
                repeatRule = p.repeatRule,
                reminderOneMinutesBefore = reminder1,
                reminderTwoMinutesBefore = reminder2,
                hasExactTime = p.hasExactTime,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
            val id = container.repository.save(item)
            container.scheduler.schedule(item.copy(id = id))
            _proposal.value = null
        }
    }

    fun markDone(id: Long, done: Boolean) = viewModelScope.launch { container.repository.markCompleted(id, done) }
    fun updateSettings(s: AppSettings) = viewModelScope.launch { container.preferences.update(s) }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(container) as T
    }
}
