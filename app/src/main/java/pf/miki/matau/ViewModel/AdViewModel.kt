package pf.miki.matau.ViewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import pf.miki.matau.Ad
import pf.miki.matau.source.AdSourceFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AdViewModel : ViewModel() {

    /**
     * The LivePagedListBuilder creates a LiveData PagedList based on the given DataFactory
     * Plus some parameters to tune the fetching like page size, prefetch size, multi thread executor
     */
    private val executor: Executor = Executors.newFixedThreadPool(2)
    private val factory = AdSourceFactory()
    val liveAds: LiveData<PagedList<Ad>> = LivePagedListBuilder(
            factory,
            PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(10)
                    .setPrefetchDistance(4)
                    .setPageSize(10).build())
            .setFetchExecutor(executor)
            .build()


    fun filterOn(query: String) {
        factory.filterOn(query)
    }

    var category: Int
        get() = factory.category
        set(value) {
            factory.category = value
        }
}
