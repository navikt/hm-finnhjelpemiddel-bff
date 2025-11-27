package no.nav.hm.finnhjelpemiddelbff.category

import jakarta.inject.Singleton

@Singleton
class CategoryService {

    suspend fun getAllCategories() {}
    suspend fun getCategory(category: String) {}
}