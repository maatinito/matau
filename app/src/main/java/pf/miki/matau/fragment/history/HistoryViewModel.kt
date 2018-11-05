package pf.miki.matau.fragment.history

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import pf.miki.matau.fragment.BaseAdViewModel
import pf.miki.matau.repository.PAd

class HistoryViewModel(app: Application) : BaseAdViewModel(app) {

    val allAds: LiveData<PagedList<PAd>> = LivePagedListBuilder(
            repository.viewedAds(),
            PagedList.Config.Builder()
                    .setEnablePlaceholders(true)
                    .setInitialLoadSizeHint(30)
                    .setPrefetchDistance(2)
                    .setPageSize(30).build())
            .setFetchExecutor(repository.executor)
            .build()



}