package com.archit.wikisearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.archit.wikisearch.adapter.SearchResultsAdapter
import com.archit.wikisearch.model.SearchResults
import com.archit.wikisearch.networkClient.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import com.archit.wikisearch.activity.WebViewActivity
import com.archit.wikisearch.adapter.OnItemClickListener
import com.archit.wikisearch.apiService.WikiSearchService
import java.util.*

class MainActivity : AppCompatActivity(), OnItemClickListener {

    val params = mutableMapOf(
        "action" to "query",
        "format" to "json",
        "prop" to "pageimages|pageterms",
        "generator" to "prefixsearch",
        "redirects" to "1",
        "formatversion" to "2",
        "piprop" to "thumbnail",
        "pithumbsize" to "50",
        "pilimit" to "10",
        "wbptterms" to "description",
        "gpslimit" to "10"
    )
    lateinit var timer: Timer
    lateinit var searchService: WikiSearchService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timer = Timer()
        searchService = RetrofitClient.getCacheEnabledRetrofit(applicationContext).create(WikiSearchService::class.java)
        rvResults.layoutManager = LinearLayoutManager(this)
        setTouchListenerOnSearchInput()
        setTextChangedListenerOnSearchInput()
    }

    /**
     * Will hit search api when input text changes
     */
    private fun setTextChangedListenerOnSearchInput() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (searchInput.text.toString().isNotEmpty()){
                    searchLayout.isErrorEnabled = false
                    hitSearchOnTypingFinished()
                }
            }
        })
    }

    /**
     * Will hit search api when user finishes typing
     * Avoids unnecessary api hit when user is typing as we should wait for
     * user to finish typing before hitting api
     */
    private fun hitSearchOnTypingFinished() {
        setProgressVisible()
        timer.cancel()
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                params.put("gpssearch", searchInput.text.toString())
                hitSearchApi(params, false, false)
            }
        }, 600)
    }

    /**
     * Will hit search api when search icon is clicked inside edit text
     */
    private fun setTouchListenerOnSearchInput() {
        searchInput.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (searchInput.right - searchInput.compoundDrawables[2].bounds.width())) {
                        if (searchInput.text.toString().isEmpty()) {
                            searchLayout.isErrorEnabled = true
                            searchLayout.error = "Search input cannot be empty"
                        } else {
                            setProgressVisible()
                            params.put("gpssearch", searchInput.text.toString())
                            hitSearchApi(params, true, true)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    /**
     * using retrofit client to hit wiki search api
     */
    private fun hitSearchApi(
        params: MutableMap<String, String>,
        hideKeyboard: Boolean,
        clearFocus: Boolean
    ) {
        searchService.getSearchResults(params).enqueue(object : Callback<SearchResults> {
            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                handleKeyboardVisibility(hideKeyboard)
                setProgressInvisible()
                Toast.makeText(this@MainActivity, "Unable to get search results", Toast.LENGTH_LONG)
                    .show()
            }
            override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
                setProgressInvisible()
                handleKeyboardVisibility(hideKeyboard)
                handleEditTextFocus(clearFocus)
                val results = response.body()
                if (results?.query == null) {
                    Toast.makeText(this@MainActivity, "No results found", Toast.LENGTH_LONG).show()
                } else {
                    rvResults.adapter =
                        SearchResultsAdapter(results?.query!!.pages, this@MainActivity)
                }
            }
        })
    }

    /**
     * handles click on result items
     * redirects to wiki page of the clicked item
     */
    override fun onResultItemClick(pageId: String) {
        intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("pageid", pageId)
        startActivity(intent)
    }

    /**
     * decides whether to clear focus from edit text
     */
    private fun handleEditTextFocus(clearFocus: Boolean) {
        if(clearFocus){
            searchLayout.clearFocus()
        }
    }

    /**
     * decides whether to hide keyboard when response comes from api
     */
    private fun handleKeyboardVisibility(hideKeyboard: Boolean) {
        if(hideKeyboard){
            hideSoftKeyboard(this@MainActivity)
            searchLayout.clearFocus()
        }
    }

    /**
     * sets progress bar to visible
     */
    private fun setProgressVisible() {
        loading_indicator.visibility = View.VISIBLE
    }

    /**
     * hides progress bar usually when response comes from api
     */
    private fun setProgressInvisible() {
        loading_indicator.visibility = View.GONE
    }

    /**
     * hides keyboard
     */
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (activity.currentFocus != null)
            inputMethodManager!!.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, 0
            )
    }
}
