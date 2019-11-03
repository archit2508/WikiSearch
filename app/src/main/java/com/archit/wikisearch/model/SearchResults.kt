package com.archit.wikisearch.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class SearchResults(
    val batchcomplete: Boolean,
    val query: Query
)

data class Query(
    val pages: List<Page>
)

@Parcelize
class Page(
    val index: Int,
    val ns: Int,
    val pageid: Int,
    val terms: Terms?,
    val thumbnail: Thumbnail?,
    val title: String
): Parcelable

@Parcelize
data class Terms(
    val description: List<String>
) : Parcelable

@Parcelize
data class Thumbnail(
    val height: Int,
    val source: String,
    val width: Int
) : Parcelable