package no.nav.hm.finnhjelpemiddelbff

import jakarta.inject.Singleton


@Singleton
class SearchService(
    private val searchClient: SearchClient
) {

    suspend fun getSeries() : String {
        return searchClient.getSeries("""
            {
    query: {
      bool: {
        must: {
          term: {
            seriesId: "de44bdb5-b6bf-4b9d-8572-6c3ad47e661a",
          },
        },
      },
    },
    size: 150,
  }
        """.trimIndent())
    }

}
