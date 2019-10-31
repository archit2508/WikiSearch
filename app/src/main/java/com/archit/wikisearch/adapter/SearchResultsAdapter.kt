package com.archit.wikisearch.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.archit.wikisearch.R
import kotlinx.android.synthetic.main.result_item.view.*
import com.archit.wikisearch.model.Page
import com.squareup.picasso.Picasso


class SearchResultsAdapter(private val pages: List<Page>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<WikiSearchHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WikiSearchHolder =
        WikiSearchHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        )

    override fun getItemCount(): Int = pages.size

    override fun onBindViewHolder(holder: WikiSearchHolder, position: Int) {
        if (pages[position].thumbnail != null)
            Picasso.get().load(pages[position].thumbnail!!.source).into(holder.itemView.imageView)
        holder.itemView.title.text = pages[position].title
        if (pages[position].terms != null)
            holder.itemView.desc.text = pages[position].terms.description[0]

        holder.itemView.setOnClickListener {
            listener.onResultItemClick(pages[position].pageid.toString())
        }
    }
}

class WikiSearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

interface OnItemClickListener {
    fun onResultItemClick(pageId: String)
}