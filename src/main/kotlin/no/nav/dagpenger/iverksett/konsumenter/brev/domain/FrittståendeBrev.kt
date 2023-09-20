package no.nav.dagpenger.iverksett.konsumenter.brev.domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.dagpenger.kontrakter.felles.FrittståendeBrevType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("frittstaende_brev")
data class FrittståendeBrev(
    @Id
    val id: UUID = UUID.randomUUID(),
    val personIdent: String,
    val eksternFagsakId: Long,
    @Column("journalforende_enhet")
    val journalførendeEnhet: String,
    val saksbehandlerIdent: String,
    @Column("stonadstype")
    val stønadstype: StønadTypeDagpenger,
    val mottakere: Brevmottakere,
    val fil: ByteArray,
    val brevtype: FrittståendeBrevType,
    val journalpostResultat: JournalpostResultatMap = JournalpostResultatMap(),
    val distribuerBrevResultat: DistribuerBrevResultatMap = DistribuerBrevResultatMap(),
    val opprettetTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
)
