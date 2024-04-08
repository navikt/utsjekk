package no.nav.utsjekk.utbetaling.util

import java.time.LocalDate

val Int.januar: LocalDate get() = LocalDate.of(2024, 1, this)

val Int.februar: LocalDate get() = LocalDate.of(2024, 2, this)

val Int.mars: LocalDate get() = LocalDate.of(2024, 3, this)

val Int.april: LocalDate get() = LocalDate.of(2024, 4, this)

val Int.mai: LocalDate get() = LocalDate.of(2024, 5, this)
