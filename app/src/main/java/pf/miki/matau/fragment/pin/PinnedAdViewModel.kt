package pf.miki.matau.fragment.pin

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import pf.miki.matau.fragment.BaseAdViewModel
import pf.miki.matau.repository.Ad
import pf.miki.matau.repository.AppDatabase
import pf.miki.matau.repository.PAd
import pf.miki.matau.repository.toPAd
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PinnedAdViewModel(app: Application) : BaseAdViewModel(app) {
    val allAds : LiveData<PagedList<PAd>> = LivePagedListBuilder(
            repository.allPinnedAdsByDate(),
            PagedList.Config.Builder()
                    .setEnablePlaceholders(true)
                    .setInitialLoadSizeHint(30)
                    .setPrefetchDistance(2)
                    .setPageSize(30).build())
            .setFetchExecutor(repository.executor)
            .build()
}
