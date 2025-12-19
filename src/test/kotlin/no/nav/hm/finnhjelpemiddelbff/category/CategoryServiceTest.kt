package no.nav.hm.finnhjelpemiddelbff.category

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@MicronautTest
class CategoryServiceTest(private val categoryService: CategoryService) {

    private val bevegelse = Category(
        id = null,
        name = "Bevegelse",
        description = "",
        subCategories = listOf(
            "Ganghjelpemidler",
            "Rullestoler",
            "Forflytning",
            "Sykler",
            "Kjelker og akebrett",
            "Vogner",
            "Bilseter og bilutstyr"
        ),
        isos = emptyList(),
        showProducts = false
    )

    private val categoryLength = 34

    @Test
    fun `Should get all categories`() {
        runBlocking {
            val categories = categoryService.getAllCategories()

            categories shouldContain bevegelse
            categories shouldHaveSize categoryLength
        }
    }

    @Test
    fun `Should find one category`() {
        runBlocking {
            categoryService.getCategory("Bevegelse") shouldBe bevegelse
        }
    }

}