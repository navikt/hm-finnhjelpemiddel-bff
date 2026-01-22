package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface CategoryRepository : CoroutineCrudRepository<CategoryDto, UUID> {
    fun findByIdInList(ids: List<UUID>): List<CategoryDto>

    fun findByTitle(title: String): CategoryDto?
}