package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory

@Controller("/category")
@Tag(name = "Categories")
class CategoryController(private val categoryService: CategoryService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CategoryController::class.java)
    }

    @Get("/categories")
    suspend fun getCategories(): HttpResponse<List<Category>> = try {
        categoryService.getAllCategories()
        HttpResponse.ok()
    } catch (exception: Exception) {
        LOG.error("Failed to get categories", exception)
        HttpResponse.serverError()
    }

    @Get("/{category}")
    suspend fun getCategory(category: String): HttpResponse<Category> = try {
        categoryService.getCategory(category)
        HttpResponse.ok()
    } catch (exception: Exception) {
        LOG.error("Failed to get category $category", exception)
        HttpResponse.serverError()
    }
}