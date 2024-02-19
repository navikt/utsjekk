package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.cucumber.domeneparser

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val norskDatoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val isoDatoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun parseDato(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
): LocalDate = parseDato(domenebegrep.nøkkel, rad)

fun parseValgfriDato(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String?>,
) = parseValgfriDato(domenebegrep.nøkkel, rad)

fun parseString(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
) = verdi(domenebegrep.nøkkel, rad)

fun parseBoolean(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
) = when (verdi(domenebegrep.nøkkel, rad)) {
    "Ja" -> true
    else -> false
}

fun parseValgfriBoolean(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String?>,
): Boolean? {
    val verdi = rad[domenebegrep.nøkkel]

    if (verdi == null || verdi == "") {
        return null
    }

    return when (verdi) {
        "Ja" -> true
        "Nei" -> false
        else -> null
    }
}

fun parseDato(
    domenebegrep: String,
    rad: Map<String, String>,
): LocalDate = parseDato(rad[domenebegrep]!!)

fun parseDato(dato: String): LocalDate =
    if (dato.contains(".")) {
        LocalDate.parse(dato, norskDatoFormatter)
    } else {
        LocalDate.parse(dato, isoDatoFormatter)
    }

fun parseValgfriDato(
    domenebegrep: String,
    rad: Map<String, String?>,
): LocalDate? {
    val verdi = rad[domenebegrep]

    if (verdi == null || verdi == "") {
        return null
    }

    return if (verdi.contains(".")) {
        LocalDate.parse(verdi, norskDatoFormatter)
    } else {
        LocalDate.parse(verdi, isoDatoFormatter)
    }
}

fun verdi(
    nøkkel: String,
    rad: Map<String, String>,
): String {
    val verdi = rad[nøkkel]

    if (verdi == null || verdi == "") {
        throw java.lang.RuntimeException("Fant ingen verdi for $nøkkel")
    }

    return verdi
}

fun valgfriVerdi(
    nøkkel: String,
    rad: Map<String, String>,
) = rad[nøkkel]

fun parseInt(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
) = Integer.parseInt(verdi(domenebegrep.nøkkel, rad).replace("_", ""))

fun parseLong(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
) = verdi(domenebegrep.nøkkel, rad).replace("_", "").toLong()

fun parseValgfriLong(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
) = parseValgfriInt(domenebegrep, rad)?.toLong()

fun parseValgfriInt(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
) = if (valgfriVerdi(domenebegrep.nøkkel, rad) == null) null else parseInt(domenebegrep, rad)

inline fun <reified T : Enum<T>> parseValgfriEnum(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
): T? {
    return enumValueOf<T>((valgfriVerdi(domenebegrep.nøkkel, rad) ?: return null).uppercase())
}

inline fun <reified T : Enum<T>> parseEnum(
    domenebegrep: Domenenøkkel,
    rad: Map<String, String>,
): T {
    return parseValgfriEnum<T>(domenebegrep, rad)!!
}
