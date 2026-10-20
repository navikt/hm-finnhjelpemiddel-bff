package no.nav.hm.finnhjelpemiddelbff.category

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@MicronautTest
class CategoryControllerTest(
    private val categoryController: CategoryController,
    private val categoryRepository: CategoryRepository,
) {

    @Test
    fun `happy path`() {
        val category = Category(title = "Kategori 1", data = CategoryData(description = "Dette er en kategori"))

        val categoryWithSubcategory = Category(title = "Kategori 2", data = CategoryData("Testert i testen"), subcategories = listOf(
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
        val category = Category(title = "Kategori 1", data = CategoryData(description = "Dette er en kategori",  icon="<svg></svg>"))

        val dataDescription = "Testert i testen"
        val dataIcon = "<svg></svg>"


        val categoryWithData = Category(title = "Kategori 2", data = CategoryData(description = dataDescription, icon=dataIcon), subcategories = listOf(
            Subcategory(id=category.id, priority = 1)
        ))

        runBlocking {
            categoryRepository.saveAll(listOf(category, categoryWithData)).toList() shouldHaveSize 2

            val responseCategoryWithData = categoryController.getCategory(categoryWithData.title)

            val subCategory =
                SubcategoryResponse(
                    category.id,
                    category.title,
                    category.data.icon,
                    category.data.description,
                    priority = 1
                )

            responseCategoryWithData.status shouldBe HttpStatus.OK
            (responseCategoryWithData.body() as CategoryOut).let {
                it.id shouldBe categoryWithData.id
                it.title shouldBe categoryWithData.title
                it.subCategories shouldBe arrayListOf(subCategory)
                it.data.description shouldBe dataDescription
                it.data.icon shouldBe dataIcon
            }
        }
    }

    @Test
    fun `isoVersion 22 returns isos22 as isos`() {
        val category = Category(
            title = "Versjonert",
            data = CategoryData(isos = listOf("1234"), isos22 = listOf("5678"), filters = listOf("Bredde"))
        )
        val withoutV22 = Category(title = "Uten v22", data = CategoryData(isos = listOf("1234")))

        runBlocking {
            categoryRepository.saveAll(listOf(category, withoutV22)).toList() shouldHaveSize 2

            (categoryController.getCategory(category.title).body() as CategoryOut).let {
                it.isoVersion shouldBe 16
                it.data.isos shouldBe listOf("1234")
            }
            (categoryController.getCategory(category.title, 22).body() as CategoryOut).let {
                it.isoVersion shouldBe 22
                it.data.isos shouldBe listOf("5678")
                it.data.filters shouldBe listOf("Bredde")
            }
            (categoryController.getCategories(listOf(withoutV22.id), 22).body() as List<*>)
                .map { (it as CategoryOut).data.isos } shouldBe listOf(emptyList<String>())

            categoryController.getCategory(category.title, 99).status shouldBe HttpStatus.BAD_REQUEST
            categoryController.getCategories(listOf(category.id), 99).status shouldBe HttpStatus.BAD_REQUEST
        }
    }
}
