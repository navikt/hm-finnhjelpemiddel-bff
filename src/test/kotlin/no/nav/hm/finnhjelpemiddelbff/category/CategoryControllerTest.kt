package no.nav.hm.finnhjelpemiddelbff.category

import tools.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class CategoryControllerTest(
    private val categoryController: CategoryController,
    private val categoryRepository: CategoryRepository,
    private val objectMapper: ObjectMapper
) {

    @Test
    fun `happy path`() {
        @Language("JSON") val data = """
            {
            "description": "Dette er en kategori"
            }
        """.trimIndent()
        val category = Category(title = "Kategori 1", data = objectMapper.readTree(data))

        @Language("JSON") val data2 = """
            {
            "description": "Testert i testen"
            }
        """.trimIndent()
        val categoryWithSubcategory = Category(title = "Kategori 2", data = objectMapper.readTree(data2), subcategories = listOf(
            Subcategory(category.id, 0)))

        runBlocking {
            categoryRepository.saveAll(listOf(category, categoryWithSubcategory)).toList() shouldHaveSize 2

            val responseCategoryWithSubcategory = categoryController.getCategory(categoryWithSubcategory.title)
            responseCategoryWithSubcategory.status shouldBe HttpStatus.OK
            (responseCategoryWithSubcategory.body() as CategoryOut).let {
                it.id shouldBe categoryWithSubcategory.id
                it.title shouldBe categoryWithSubcategory.title
                it.subCategories shouldHaveSize 1
            }

            val responseCategoryDto = categoryController.getCategory(category.title)
            responseCategoryDto.status shouldBe HttpStatus.OK
            (responseCategoryDto.body() as CategoryOut).let {
                it.id shouldBe category.id
                it.title shouldBe category.title
                it.subCategories shouldBe emptyList()
            }

            val responseCategoryList = categoryController.getCategories(listOf(category.id, categoryWithSubcategory.id))
            responseCategoryList.status shouldBe HttpStatus.OK
            (responseCategoryList.body() as List<*>).size shouldBe 2
        }
    }

    @Test
    fun `bad id`() {
        runBlocking {
            categoryController.getCategory("unknown").status shouldBe HttpStatus.BAD_REQUEST
        }
    }

    @Test
    fun `data content`() {
        @Language("JSON") val dataSub = """
            {
            "description": "Dette er en kategori",
            "icon": "<svg></svg>"
            }
        """.trimIndent()
        val category = Category(title = "Kategori 1", data = objectMapper.readTree(dataSub))

        val dataDescription = "Testert i testen"
        val dataSubCategories = "${category.id}"
        val dataIcon = "<svg></svg>"

        @Language("JSON") val data = """
            {
            "description": "$dataDescription",
            "icon": "$dataIcon"
            }
        """.trimIndent()
        val categoryWithData = Category(title = "Kategori 2", data = objectMapper.readTree(data), subcategories = listOf(
            Subcategory(id=category.id, priority = 1)
        ))

        runBlocking {
            categoryRepository.saveAll(listOf(category, categoryWithData)).toList() shouldHaveSize 2

            val responseCategoryWithData = categoryController.getCategory(categoryWithData.title)

            val subCategory =
                SubcategoryResponse(
                    category.id,
                    category.title,
                    category.data["icon"].asString().orEmpty(),
                    category.data["description"].asString().orEmpty(),
                    priority = 1
                )

            responseCategoryWithData.status shouldBe HttpStatus.OK
            (responseCategoryWithData.body() as CategoryOut).let {
                it.id shouldBe categoryWithData.id
                it.title shouldBe categoryWithData.title
                it.subCategories shouldBe arrayListOf(subCategory)
                it.data["description"].asString() shouldBe dataDescription
                it.data["icon"].asString() shouldBe dataIcon
            }
        }
    }
}