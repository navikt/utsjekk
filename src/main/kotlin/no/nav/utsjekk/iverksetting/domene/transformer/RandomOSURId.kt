package no.nav.utsjekk.iverksetting.domene.transformer

import kotlin.random.Random

object RandomOSURId {
    private val chars: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generate() =
        (1..20)
            .map { Random.nextInt(0, chars.size).let { chars[it] } }
            .joinToString("")
}
