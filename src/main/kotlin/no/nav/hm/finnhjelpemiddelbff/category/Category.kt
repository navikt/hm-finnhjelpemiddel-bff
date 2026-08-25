package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID
import tools.jackson.databind.JsonNode

@Serdeable
@MappedEntity("category")
data class Category(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val data: JsonNode,
    val created: LocalDateTime = LocalDateTime.now(),
    @field:TypeDef(type = DataType.JSON)
    val subcategories: List<Subcategory> = emptyList()
)

@Serdeable
data class Subcategory(
    val id: UUID,
    val priority: Int,
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
    val subCategories: List<SubcategoryResponse>,
    val data: JsonNode
)

@Serdeable
data class SubcategoryResponse(val id: UUID, val title: String, val icon: String?, val description: String?, val priority: Int? = 0)