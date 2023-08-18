package no.nav.dagpenger.iverksett.api.domene

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.UUID

data class SakIdentifikator(val sakId: UUID?, val saksreferanse: String?) {
     init {
         if (this.sakId == null && this.saksreferanse == null) {
             throw IllegalArgumentException("SakIdentifikator må ha enten sakId eller saksreferanse")
         }
     }

    constructor(sakId: UUID): this(sakId, null)
    constructor(saksreferanse: String): this(null, saksreferanse)

    fun toIdString(): String {
        return sakId?.toString() ?: saksreferanse!!
    }
}

fun String.toSakIdentifikator(): SakIdentifikator {
    val id = this
    return Result.runCatching { UUID.fromString(id) }.fold(
        onSuccess = { SakIdentifikator(it, null) },
        onFailure = { SakIdentifikator(null, id) }
    )
}
