package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
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
        HttpResponse.serverError(exception.message)
    }

    private fun CategoryDto.toOut(): CategoryOut = CategoryOut(
        id = id,
        title = title,
        subCategories =
            categoryRepository.findByIdInList(
                data["subCategories"]?.map { UUID.fromString(it.asText()) }.orEmpty()
            ).map { SubCategory(it.id, it.title, it.data["icon"]?.asText().orEmpty()) },
        data = data
    )
}