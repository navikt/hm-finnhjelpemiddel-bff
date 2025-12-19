package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.JsonDataType
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
data class Category(
    val id: UUID? = UUID.randomUUID(),
    val name: String,
    val description: String?,
    val subCategories: List<String>?,
    val isos: List<String>?,
    val showProducts: Boolean
)

@Serdeable
@MappedEntity("category")
data class CategoryDto(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val data: JsonDataType,
    val created: LocalDateTime = LocalDateTime.now()
)