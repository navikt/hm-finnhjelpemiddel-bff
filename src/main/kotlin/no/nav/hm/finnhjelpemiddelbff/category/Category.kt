package no.nav.hm.finnhjelpemiddelbff.category

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Category(
    val name: String,
    val description: String,
    val subCategories: List<Category>,
    val isos: List<String>,
    val showProducts: Boolean
)