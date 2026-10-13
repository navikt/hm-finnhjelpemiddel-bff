package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@MappedEntity("category")
data class Category(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val data: CategoryData,
    val created: LocalDateTime = LocalDateTime.now(),
    @field:TypeDef(type = DataType.JSON)
    val subcategories: List<Subcategory>? = emptyList()
)

@Serdeable
data class CategoryData(
    val description: String? = null,
    val isos: List<String>? = null,
    val isos22: List<String>? = null,
    val filters: List<String>? = null,
    val icon: String? = null,
    val showSubCategoryIcons: Boolean? = null
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
    val data: CategoryData,
)

@Serdeable
data class CategoryOut(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
    val subCategories: List<SubcategoryResponse>,
    val data: CategoryData
)

@Serdeable
data class SubcategoryResponse(val id: UUID, val title: String, val icon: String?, val description: String?, val priority: Int? = 0)