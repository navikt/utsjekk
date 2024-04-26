package no.nav.utsjekk.utbetaling.featuretoggle

import no.nav.utsjekk.kontrakter.felles.Fagsystem

class IverksettingErSkruddAvException(fagsystem: Fagsystem) :
    IllegalStateException("Iverksetting er skrudd av for fagsystem $fagsystem")
