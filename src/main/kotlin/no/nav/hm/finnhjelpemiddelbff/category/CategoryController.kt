package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
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
        private const val ISO_VERSION_16 = 16
        private const val ISO_VERSION_22 = 22
        private val SUPPORTED_ISO_VERSIONS = setOf(ISO_VERSION_16, ISO_VERSION_22)
    }

    @Get("/{category}")
    fun getCategory(category: String, @QueryValue isoVersion: Int? = null): HttpResponse<*> = try {
        withIsoVersion(isoVersion) { version ->
            categoryRepository.findByTitle(category)?.let { HttpResponse.ok(it.toOut(version)) }
                ?: HttpResponse.badRequest("No category with id $category")
        }
    } catch (exception: Exception) {
        LOG.error("Error when getting category $category", exception)
        HttpResponse.serverError(exception.message!!)
    }

    @Post("/ids")
    fun getCategories(@Body categories: List<UUID>, @QueryValue isoVersion: Int? = null): HttpResponse<*> = try {
        withIsoVersion(isoVersion) { version ->
            HttpResponse.ok(categories.mapNotNull { runBlocking { categoryRepository.findById(it)?.toOut(version) } })
        }
    } catch (exception: Exception) {
        LOG.error("Error when getting categories $categories", exception)
        HttpResponse.serverError(exception.message!!)
    }

    private fun withIsoVersion(isoVersion: Int?, block: (Int) -> HttpResponse<*>): HttpResponse<*> {
        val version = isoVersion ?: ISO_VERSION_16
        return if (version in SUPPORTED_ISO_VERSIONS) block(version)
        else HttpResponse.badRequest("Unsupported isoVersion $version")
    }

    private fun Category.toOut(isoVersion: Int): CategoryOut = CategoryOut(
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
                icon = it.data.icon,
                description = it.data.description,
                priority = priorities[it.id]?.priority
            ) }

        }.orEmpty(),
        // Version 22 is returned in the v16 shape so clients read `isos` for either version.
        data = if (isoVersion == ISO_VERSION_22) data.copy(isos = data.isos22.orEmpty()) else data,
        isoVersion = isoVersion
    )
}