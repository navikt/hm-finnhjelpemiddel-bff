package no.nav.hm.finnhjelpemiddelbff.category.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.hm.finnhjelpemiddelbff.category.CategoryDto
import no.nav.hm.finnhjelpemiddelbff.category.CategoryRepository
import org.slf4j.LoggerFactory


@Controller("/admin/category")
@Tag(name = "Category administration")
class CategoryAdminController(private val categoryRepository: CategoryRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CategoryAdminController::class.java)
    }

    @Post("")
    suspend fun createCategory(@Body categoryDto: CategoryDto): HttpResponse<String> = try {
        categoryRepository.save(categoryDto)
        HttpResponse.ok()
    } catch (exception: Exception) {
        LOG.error("Failed to create new category \"$categoryDto\"", exception)
        HttpResponse.serverError()
    }

    @Post("")
    suspend fun updateCategory(@Body categoryDto: CategoryDto): HttpResponse<String> = try {
        categoryRepository.update(categoryDto)
        HttpResponse.ok()
    } catch (exception: Exception) {
        LOG.error("Failed to update category \"$categoryDto\"", exception)
        HttpResponse.serverError()
    }
}
