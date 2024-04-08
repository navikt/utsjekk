package no.nav.utsjekk.util

import java.nio.charset.StandardCharsets

object ResourceLoaderTestUtil {
    fun readResource(name: String): String {
        return this::class.java.classLoader.getResource(name)!!.readText(StandardCharsets.UTF_8)
    }
}
