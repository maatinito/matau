package pf.miki.matau.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.Context
import pf.miki.matau.fragment.ads2.NetworkState
import pf.miki.matau.fragment.ads2.SourceParameters
import pf.miki.matau.helpers.Alias
import pf.miki.matau.source.AdSourceFactory
import pf.miki.matau.source.Category
import pf.miki.matau.source.SourceType
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

fun PAd.toAd(): Ad {
    val ad = Ad(source, id, title, fcpPrice, vignette)
    ad.liveDate.value = date
    ad.liveContact.value = contact
    ad.liveDescription.value = description
    ad.liveImages.value = images.split(" ")
    ad.lastViewed = lastViewed
    ad.pinned = pinned
    return ad
}

fun Ad.toPAd(): PAd {
    val date: Date = liveDate.value ?: Date()
    val pad = PAd(source, id, title, fcpPrice, vignette, liveDescription.value
            ?: "", date, liveContact.value ?: "", liveLocation.value ?: "")
    pad.imageList = liveImages.value ?: listOf()
    pad.lastViewed = lastViewed
    pad.pinned = pinned
    return pad
}


class AdRepository(val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    val executor: Executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() - 1))

    private val factory = AdSourceFactory(context)

    val liveAds: LiveData<PagedList<PAd>> = LivePagedListBuilder(
            factory,
            PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(10)
                    .setPrefetchDistance(4)
                    .setPageSize(10).build())
            .setFetchExecutor(executor)
            .build()
    val liveNetworkState: LiveData<NetworkState> = Transformations.switchMap(factory.source) { it.networkState }
    val liveRefreshState: LiveData<NetworkState> = Transformations.switchMap(factory.source) { it.refreshState }
    val liveParameters: MutableLiveData<SourceParameters> by Alias(factory::liveParameters)
    val liveUpdatedAds: LiveData<List<PAd>> = Transformations.switchMap(factory.source) { it.updatedAds }

    fun pin(ad: PAd, checked: Boolean) {
        ad.pinned = checked
//        ad.lastViewed = Date()
        update(ad)
    }

    fun update(ad: PAd) {
        executor.execute {
            db.pAdDao().update(ad)
            factory.source.value?.updatedAds?.postValue(listOf(ad))
        }
    }

    fun adViewed(ad: PAd) {
        ad.lastViewed = Date()
        update(ad)
    }

    fun loadAd(id: String): LiveData<PAd> {
        return db.pAdDao().load(id)
    }

    fun viewedAds(): DataSource.Factory<Int, PAd> {
        return db.pAdDao().viewedAds()
    }

    fun allPinnedAdsByDate(): DataSource.Factory<Int, PAd> {
        return db.pAdDao().allPinnedAdsByDate()
    }

    fun computeMaxPrice(): LiveData<Int> {
        return db.pAdDao().getMaxPrice()
    }

    fun maintenance() {
        executor.execute {
            val oldMonths = -1
            // remove ads created a logn time ago to clean the database
            val date = Calendar.getInstance()
            date.roll(Calendar.MONTH, oldMonths)
            db.pAdDao().deleteOldAds(date.time)
        }
    }

    var category: Category
        get() = liveParameters.value?.category ?: Category.puericulture
        set(v) {
            with(liveParameters) {
                if (v != value?.category)
                    value = value?.new(v) ?: SourceParameters(sourceType, v, search)
            }
        }

    var search: String
        get() = liveParameters.value?.search ?: ""
        set(v) {
            with(liveParameters) {
                if (v != value?.search)
                    value = value?.new(v) ?: SourceParameters(sourceType, category, v)
            }
        }

    var sourceType: SourceType
        get() = liveParameters.value?.sourceType ?: SourceType.PETITES_ANNONCES
        set(v) {
            with(liveParameters) {
                if (v != value?.sourceType)
                    value = value?.new(v) ?: SourceParameters(v, category, search)
            }
        }

    companion object {
        private var INSTANCE: AdRepository? = null

        fun getRepository(context: Context): AdRepository {
            if (AdRepository.INSTANCE == null) {
                synchronized(AdRepository::class.java) {
                    if (AdRepository.INSTANCE == null) {
                        AdRepository.INSTANCE = AdRepository(context)
                    }
                }
            }

            return AdRepository.INSTANCE!!
        }

    }

}