package pf.miki.matau.fragment.ads

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import pf.miki.matau.helpers.Alias
import pf.miki.matau.repository.AppDatabase
import pf.miki.matau.repository.PAd
import pf.miki.matau.source.AdSourceFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AdViewModel(app: Application) : AndroidViewModel(app) {

    /**
     * The LivePagedListBuilder creates a LiveData PagedList based on the given DataFactory
     * Plus some liveParameters to tune the fetching like page size, prefetch size, multi thread executor
     */
    private val executor: Executor = Executors.newFixedThreadPool(2)
    private val factory = AdSourceFactory(app)
    val liveAds: LiveData<PagedList<PAd>> = LivePagedListBuilder(
            factory,
            PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(10)
                    .setPrefetchDistance(4)
                    .setPageSize(10).build())
            .setFetchExecutor(executor)
            .build()


    private val db = AppDatabase.getDatabase(getApplication())

    fun pin(ad: PAd, pinned: Boolean) {
        ad.pinned = pinned;
        executor.execute {
            db.pAdDao().insert(ad)
        }
    }

    var category by Alias(factory::category)
    var sourceType by Alias(factory::sourceType)
    var search by Alias(factory::search)
}

