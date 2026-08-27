package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Controller("/category")
@Tag(name = "Categories")
class CategoryController(
    private val categoryRepository: CategoryRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(CategoryController::class.java)
    }

    @Get("/{category}")
    fun getCategory(category: String): HttpResponse<*> = try {
        categoryRepository.findByTitle(category)?.let { HttpResponse.ok(it.toOut()) }
            ?: HttpResponse.badRequest("No category with id $category")
    } catch (exception: Exception) {
        LOG.error("Error when getting category $category", exception)
        HttpResponse.serverError(exception.message!!)
    }

    @Post("/ids")
    fun getCategories(@Body categories: List<UUID>): HttpResponse<*> = try {
        HttpResponse.ok(categories.mapNotNull { runBlocking { categoryRepository.findById(it)?.toOut() } })
    } catch (exception: Exception) {
        LOG.error("Error when getting categories $categories", exception)
        HttpResponse.serverError(exception.message!!)
    }

    private fun Category.toOut(): CategoryOut = CategoryOut(
        id = id,
        title = title,
        subCategories = subcategories?.let { subcategories ->
            val categories = categoryRepository.findByIdInList(
                subcategories.map { it.id }
            )
            val priorities = subcategories.associateBy { it.id }

            categories.map { SubcategoryResponse(
                id = it.id,
                title = it.title,
                icon = it.data["icon"]?.asString().orEmpty(),
                description = it.data["description"]?.asString().orEmpty(),
                priority = priorities[it.id]?.priority
            ) }

        }.orEmpty(),
        data = data
    )
}