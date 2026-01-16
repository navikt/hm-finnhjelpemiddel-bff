package no.nav.hm.finnhjelpemiddelbff.category

import com.fasterxml.jackson.databind.JsonNode
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
data class Category(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
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
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val data: JsonNode,
    val created: LocalDateTime = LocalDateTime.now()
)

@Serdeable
data class CreateCategoryDto(
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val data: JsonNode,
)