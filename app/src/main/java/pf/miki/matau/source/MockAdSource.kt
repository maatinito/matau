package pf.miki.matau.source

import android.util.Log
import pf.miki.matau.Ad
import pf.miki.matau.BuildConfig
import java.util.*

class MockAdSource(filter: String, category: Category) : AdSource(filter, category) {
    override fun setDetail(ad: Ad) {
        ad.liveDescription.postValue("description ${ad.id}")
    }

    companion object {
        val TAG: String = this::class.java.simpleName
        const val MAX_PAGE_SIZE = 10

    }

    /**
     * The list of items in the data source
     */
    private val items: MutableList<Ad> = ArrayList()

    init {
        // Create some fake data
        for (i in 0..200) {
            items += Ad("MockSource", i.toString(), "Title $i", i * 1234 * 120 / 1000, "photo/1053730.jpg")
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadInitial: placeHolder=${params.placeholdersEnabled}, size=${params.requestedLoadSize}")
        callback.onResult(subList(1, params.requestedLoadSize), null, 2)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val subList = subList(params.key, params.requestedLoadSize)
        val pageSize = minOf(MAX_PAGE_SIZE, params.requestedLoadSize)
        val end = params.key * pageSize
        callback.onResult(subList, if (end < items.size) params.key + 1 else null)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.d(TAG, "loadBefore: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val subList = subList(params.key, params.requestedLoadSize)
        callback.onResult(subList, if (params.key > 1) params.key - 1 else null)
    }

    private fun subList(key: Int, requestedLoadSize: Int): MutableList<Ad> {
        val pageSize = minOf(MAX_PAGE_SIZE, requestedLoadSize)
        val start = (key - 1) * pageSize
        val end = minOf(start + pageSize, items.size)
        return items.subList(start, end)
    }
}