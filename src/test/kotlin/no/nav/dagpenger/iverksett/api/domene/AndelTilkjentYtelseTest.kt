package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import java.time.LocalDate

internal class AndelTilkjentYtelseTest {

    private fun lagTY(beløp: Int, inntektsreduksjon: Int = 0, samordningsfradrag: Int = 0) =
        lagAndelTilkjentYtelse(
            beløp = beløp,
            fraOgMed = LocalDate.now(),
            tilOgMed = LocalDate.now(),
        )
}
