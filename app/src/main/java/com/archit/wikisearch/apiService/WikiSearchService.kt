package com.archit.wikisearch.apiService

import com.archit.wikisearch.model.SearchResults
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface WikiSearchService {

    @GET("w/api.php")
    fun getSearchResults(@QueryMap params: Map<String, String>): Call<SearchResults>
}