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

@Serdeable
data class CategoryOut(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
    val subCategories: List<SubCategory>,
    val data: JsonNode
)

@Serdeable
data class SubCategory(val id: UUID, val title: String)