package no.nav.hm.finnhjelpemiddelbff

import jakarta.inject.Singleton


@Singleton
class SearchService(
    private val searchClient: SearchClient
) {

    suspend fun getSeries(seriesId: String) : String {
        return searchClient.getSeries("""
            {
                "query": {
                    "bool": {
                        "must": {
                            "term": {
                                "seriesId": "$seriesId"
                            }
                        }
                    }
                },
                "size": 150
            }
            """.trimIndent())
    }

}
