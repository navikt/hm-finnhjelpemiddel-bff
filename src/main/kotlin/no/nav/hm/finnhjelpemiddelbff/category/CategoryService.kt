package no.nav.hm.finnhjelpemiddelbff.category

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
class CategoryService(private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CategoryService::class.java)
    }

    private var categories: List<Category>

    init {
        runBlocking {
            categories = objectMapper.readValue(
                this::class.java.getResourceAsStream("/categories/categories-bevegelse.json"),
                object : TypeReference<List<Category>>() {})
            LOG.info("Init categories, size ${categories.size}")
        }
    }

    fun getAllCategories(): List<Category> = categories

    fun getCategory(category: String) = categories.find { it.title == category }
}