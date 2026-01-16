package no.nav.hm.finnhjelpemiddelbff.category.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelbff.auth.AuthBody
import no.nav.hm.finnhjelpemiddelbff.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelbff.category.CategoryDto
import no.nav.hm.finnhjelpemiddelbff.category.CategoryRepository
import no.nav.hm.finnhjelpemiddelbff.category.CreateCategoryDto
import org.slf4j.LoggerFactory
import java.util.UUID


@Controller("/admin/category")
@Tag(name = "Category administration")
class CategoryAdminController(
    private val categoryRepository: CategoryRepository,
    private val azureAdUserClient: AzureAdUserClient
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CategoryAdminController::class.java)
    }

    @Post("/")
    suspend fun createCategory(
        @Header("Authorization") authorization: String,
        @Body newCategoryDto: CreateCategoryDto
    ): HttpResponse<String> =
        authenticated(authorization) {
            try {
                val category = runBlocking {
                    categoryRepository.save(CategoryDto(title=newCategoryDto.title, data=newCategoryDto.data))
                }
                HttpResponse.ok(category.id.toString())
            } catch (exception: Exception) {
                LOG.error("Failed to create new category \"$newCategoryDto\"", exception)
                HttpResponse.serverError()
            }
        }

    @Put("/")
    suspend fun updateCategory(
        @Header("Authorization") authorization: String,
        @Body categoryDto: CategoryDto
    ): HttpResponse<String> =
        authenticated(authorization) {
            try {
                runBlocking {
                    categoryRepository.update(categoryDto)
                }
                HttpResponse.ok()
            } catch (exception: Exception) {
                LOG.error("Failed to update category \"$categoryDto\"", exception)
                HttpResponse.serverError()
            }

        }

    @Get("/id/{id}")
    suspend fun getCategoryById(id: String) = categoryRepository.findById(UUID.fromString(id))

    @Delete("/id/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") authorization: String,
        id: String
    ): HttpResponse<String> = authenticated(authorization) {
        try {
            runBlocking {
                categoryRepository.deleteById(UUID.fromString(id))
            }
            HttpResponse.ok()
        } catch (exception: Exception) {
            LOG.error("Failed to delete category \"$id\"", exception)
            HttpResponse.serverError()
        }
    }


    @Get("/")
    suspend fun getCategories() = categoryRepository.findAll().toList()

    private suspend fun authenticated(authorization: String, body: () -> HttpResponse<String>): HttpResponse<String> =
        azureAdUserClient.validateToken(AuthBody(token = authorization.removePrefix("Bearer "))).let {
            if (it.active) {
                return body()
            } else {
                LOG.warn("Token fail: " + it.error)
                return HttpResponse.unauthorized()
            }
        }
}
