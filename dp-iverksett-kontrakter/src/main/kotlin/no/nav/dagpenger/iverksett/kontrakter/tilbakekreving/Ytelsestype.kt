package no.nav.dagpenger.iverksett.kontrakter.tilbakekreving

import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.iverksett.kontrakter.felles.Tema

enum class Ytelsestype(val kode: String, val navn: Map<Språkkode, String>) {
    DAGPENGER(
        "DP",
        mapOf(
            Språkkode.NB to "Dagpenger",
            Språkkode.NN to "Dagpengar",
        ),
    )
}