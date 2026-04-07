package cl.javier.agendaclara.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TextProposalParserTest {
    private val parser = TextProposalParser()

    @Test
    fun `jueves usa futuro incluso si hoy es jueves`() {
        val today = LocalDate.of(2026, 4, 9) // jueves
        val result = parser.parse("jueves", today)
        assertEquals(LocalDate.of(2026, 4, 16), result.startDate)
    }

    @Test
    fun `mañana en la mañana sugiere ocho`() {
        val result = parser.parse("recordar mañana en la mañana", LocalDate.of(2026, 4, 7))
        assertEquals(LocalTime.of(8, 0), result.suggestedTime)
    }
}
