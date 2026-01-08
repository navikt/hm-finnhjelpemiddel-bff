package no.nav.hm.finnhjelpemiddelbff.category.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.hm.finnhjelpemiddelbff.auth.AuthBody
import no.nav.hm.finnhjelpemiddelbff.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelbff.category.CategoryDto
import no.nav.hm.finnhjelpemiddelbff.category.CategoryRepository
import org.slf4j.LoggerFactory


@Controller("/admin/category")
@Tag(name = "Category administration")
class CategoryAdminController(
    private val categoryRepository: CategoryRepository,
    private val azureAdUserClient: AzureAdUserClient
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CategoryAdminController::class.java)
    }

    @Post("")
    suspend fun createCategory(
        @Header("Authorization") authorization: String,
        @Body categoryDto: CategoryDto
    ): HttpResponse<String> {
        val authToken = authorization.removePrefix("Bearer ")

        val tokenValidated = azureAdUserClient.validateToken(AuthBody(token = authToken))

        if (tokenValidated.active) {
            try {
                categoryRepository.save(categoryDto)
                return HttpResponse.ok()
            } catch (exception: Exception) {
                LOG.error("Failed to create new category \"$categoryDto\"", exception)
                return HttpResponse.serverError()
            }
        } else {
            LOG.warn("Token fail: " + tokenValidated.error)
            return HttpResponse.unauthorized()
        }
    }

    @Put("")
    suspend fun updateCategory(
        @Header("Authorization") authorization: String,
        @Body categoryDto: CategoryDto
    ): HttpResponse<String> {
        val authToken = authorization.removePrefix("Bearer ")

        val tokenValidated = azureAdUserClient.validateToken(AuthBody(token = authToken))

        if (tokenValidated.active) {
            try {
                categoryRepository.update(categoryDto)
                return HttpResponse.ok()
            } catch (exception: Exception) {
                LOG.error("Failed to update category \"$categoryDto\"", exception)
                return HttpResponse.serverError()
            }
        } else {
            LOG.warn("Token fail: " + tokenValidated.error)
            return HttpResponse.unauthorized()
        }
    }
}
