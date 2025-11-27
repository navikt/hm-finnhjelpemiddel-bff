package no.nav.hm.finnhjelpemiddelbff.iso

import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton

@Singleton
class IsoService(private val grunndataDbClient: GrunndataDbClient) {
    suspend fun getIsoTree(): IsoTree = grunndataDbClient.getIsos().toIsoTree()
}

fun IsoResponse.toIso(): Iso = Iso(isoCode, isoTitle, isoText, isoTextShort, isoLevel)

fun List<IsoResponse>.toIsoTree(): IsoTree = IsoTree(associate { it.isoCode to it.toIso() })

@Serdeable
data class IsoTree(val isos: Map<String, Iso>)

@Serdeable
data class Iso(
    val isoCode: String,
    val isoTitle: String,
    val isoText: String,
    val isoTextShort: String,
    val isoLevel: Int
)
