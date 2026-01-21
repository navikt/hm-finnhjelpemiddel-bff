package no.nav.hm.finnhjelpemiddelbff.category

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.util.UUID
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
        val categoryDto = CategoryDto(title = "Kategori 1", data = objectMapper.readTree(data))

        @Language("JSON") val data2 = """
            {
            "description": "Testert i testen",
            "subCategories": ["${categoryDto.id}"]
            }
        """.trimIndent()
        val categoryWithSubcategory = CategoryDto(title = "Kategori 2", data = objectMapper.readTree(data2))

        runBlocking {
            categoryRepository.saveAll(listOf(categoryDto, categoryWithSubcategory)).toList() shouldHaveSize 2

            val responseCategoryWithSubcategory = categoryController.getCategory(categoryWithSubcategory.id.toString())
            responseCategoryWithSubcategory.status shouldBe HttpStatus.OK
            (responseCategoryWithSubcategory.body() as CategoryOut).let {
                it.id shouldBe categoryWithSubcategory.id
                it.title shouldBe categoryWithSubcategory.title
                it.subCategories shouldHaveSize 1
            }

            val responseCategoryDto = categoryController.getCategory(categoryDto.id.toString())
            responseCategoryDto.status shouldBe HttpStatus.OK
            (responseCategoryDto.body() as CategoryOut).let {
                it.id shouldBe categoryDto.id
                it.title shouldBe categoryDto.title
                it.subCategories shouldBe emptyList()
            }
        }
    }

    @Test
    fun `bad id`() {
        runBlocking {
            categoryController.getCategory(UUID.randomUUID().toString()).status shouldBe HttpStatus.BAD_REQUEST
        }
    }
}