package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.Opplysningskilde
import no.nav.dagpenger.iverksett.kontrakter.felles.Revurderingsårsak

data class ÅrsakRevurderingDto(
    val opplysningskilde: Opplysningskilde,
    val årsak: Revurderingsårsak,
)
