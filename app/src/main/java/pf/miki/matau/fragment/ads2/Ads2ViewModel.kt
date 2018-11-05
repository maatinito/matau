package pf.miki.matau.fragment.ads2

import android.app.Application
import android.arch.lifecycle.*
import android.arch.paging.PagedList
import pf.miki.matau.repository.*
import pf.miki.matau.source.Category
import pf.miki.matau.source.SourceType

data class SourceParameters(val sourceType: SourceType, val category: Category, val search: String) {
    fun new(c: Category) = SourceParameters(sourceType, c, "")
    fun new(s: String) = SourceParameters(sourceType, category, s)
    fun new(s: SourceType) = SourceParameters(s, category, search)
}

enum class Status {
    RUNNING,
    SUCCESS,
    FAILED
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: Status,
        val msg: String? = null) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        fun error(msg: String?) = NetworkState(Status.FAILED, msg)
    }
}

data class Listing<T>(
        // the LiveData of paged lists for the UI to observe
        val pagedList: LiveData<PagedList<T>>,
        // represents the network request status to show to the user
        val networkState: LiveData<NetworkState>,
        // represents the refresh status to show to the user. Separate from networkState, this
        // value is importantly only when refresh is requested.
        val refreshState: LiveData<NetworkState>)
/*,
        // refreshes the whole data and fetches it from scratch.
        val refresh: () -> Unit,
        // retries any failed requests.
        val retry: () -> Unit)
        */

class Ads2ViewModel(app: Application) : AndroidViewModel(app) {
    val repository = AdRepository.getRepository(getApplication())
    val parameters = repository.liveParameters

    var category: Category
        get() = parameters.value?.category ?: Category.puericulture
        set(value) {
            if (value != parameters.value?.category)
                parameters.value = parameters.value?.new(value) ?: SourceParameters(sourceType, value, search)
        }

    var search: String
        get() = parameters.value?.search ?: ""
        set(value) {
            if (value != parameters.value?.search)
                parameters.value = parameters.value?.new(value) ?: SourceParameters(sourceType, category, value)
        }

    var sourceType: SourceType
        get() = parameters.value?.sourceType ?: SourceType.PETITES_ANNONCES
        set(value) {
            if (value != parameters.value?.sourceType)
            parameters.value = parameters.value?.new(value) ?: SourceParameters(value, category, search)
    }

    val ads: LiveData<PagedList<PAd>> = repository.liveAds
    val networkState: LiveData<NetworkState> = repository.liveNetworkState
    val refreshState: LiveData<NetworkState> = repository.liveRefreshState
    val updatedAds: LiveData<List<PAd>> = repository.liveUpdatedAds

    fun refresh() {
        //listing.value?.refresh?.invoke()
    }

    fun retry() {
        //listing.value?.retry?.invoke()
    }

}
