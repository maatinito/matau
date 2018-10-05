package pf.miki.matau.source

import android.arch.paging.PageKeyedDataSource
import android.util.Log
import pf.miki.matau.Ad
import android.arch.paging.DataSource
import android.os.SystemClock
import com.github.kittinunf.fuel.Fuel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pf.miki.matau.BuildConfig
import java.util.*


abstract class AdSource(var filter: String, var category: String) : PageKeyedDataSource<Int, Ad>() {
    abstract fun setDetail(ad: Ad)
}

//--------------------------------------------- Mock Source for testing purposes

class MockAdSource(filter: String, category: String) : AdSource(filter, category) {
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
            items += Ad(i.toString(), "Mock Source", "Title $i", i * 1.234f, i * 1234 * 120 / 1000, "photo/1053730.jpg")
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

//---------------------------------------------------------- Petite Annonces source

class PAAdSource(filter: String, category: String) : AdSource(filter, category) {


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadInitial: placeHolder=${params.placeholdersEnabled}, size=${params.requestedLoadSize}")
        val ads = loadPage(1)
        callback.onResult(ads, null, 2)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val ads = loadPage(params.key)
        callback.onResult(ads, if (params.key < 10) params.key + 1 else null)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val ads = loadPage(params.key)
        callback.onResult(ads, if (params.key > 1) params.key - 1 else null)
    }


    private fun loadPage(pageKey: Int): MutableList<Ad> {
        val url = if (filter.isEmpty()) baseUrl else searchUrl
        val params = mutableListOf<Pair<String, String>>()
        params.add(CATEGORY to category)
        if (filter.isNotEmpty())
            params.add(QUERY to filter)
        params.add(PAGE to pageKey.toString())

        val ads = ArrayList<Ad>()
        val get = Fuel.get(url, params)
//        Log.i("AdSource", "Requete: ${get.url}")
        val (_, _, result) = get.responseString()
        result.fold({ d ->
            //            Log.i("Mt_document? ", "${d.length} $d")
            var r = anchor.find(d)
            while (r != null) {
                val numero = r.groups[1]!!.value
                val imgMatch = imgre.find(d, r.range.last)
                if (imgMatch != null) {
                    val img = imgMatch.groups[1]!!.value
                    val titleMatch = titlere.find(d, imgMatch.range.last)
                    if (titleMatch != null) {
                        val title = titleMatch.groups[1]!!.value
                        val priceMatch = pricere.find(d, titleMatch.range.last)
                        if (priceMatch != null) {
                            val xpfPrice = priceMatch.groups[1]!!.value.replace(" ", "").toInt()
                            val euroPrice = priceMatch.groups[2]!!.value.replace(" ", "").replace(',', '.').toFloat()
                            if (BuildConfig.DEBUG) Log.i("Mt_Ad=", "$numero -> $img,$title,$xpfPrice,$euroPrice")
                            val ad = Ad(numero, baseUrl, title, euroPrice, xpfPrice, img)
                            setDetail(ad)
                            ads.add(ad)
                        }
                    }
                }
                r = r.next()
            }
        }, { err ->
            Log.e("Mt_error", err.toString())
        })
        return ads
    }

    private fun computeDetail(ad: Ad) {
        val url = detailUrl
        val params = mutableListOf<Pair<String, String>>()
        params.add(AD to ad.id)

        val get = Fuel.get(url, params)
        get.responseString { request, response, result ->
            //do something with response
            result.fold({ d ->
                //do something with data
            }, { err ->
                //do something with error
            })
        }
//        Log.i("AdSource", "Requete: ${get.url}")
        get.responseString { request, response, result ->
            result.fold({ d ->
                val dateMatch = datere.find(d)
                if (dateMatch != null) {
                    val date = dateMatch.groups[1]!!.value
                    ad.liveDate.postValue(date)
                }
                val locationMatch = locationre.find(d)
                if (locationMatch != null) {
                    val location = locationMatch.groups[1]!!.value
                    ad.liveLocation.postValue(location)
                }
                val descriptifMatch = descriptifre.find(d)
                if (descriptifMatch != null) {
                    val descriptif = descriptifMatch.groups[1]!!.value
                    ad.liveDescription.postValue(descriptif)
                }
                val contactMatch = contactre.find(d)
                if (contactMatch != null) {
                    val contact = contactMatch.groups[1]!!.value
                    ad.liveContact.postValue(contact)
                }
            }, { err ->
                Log.e("Mt_error", err.toString())
            })
        }
    }

    override fun setDetail(ad: Ad) {
        val start = System.currentTimeMillis()
//        val obs = Single.create<Ad> { emitter ->
        computeDetail(ad)
//            emitter.onSuccess(ad)
//        }
//        obs.subscribeOn(Schedulers.io())
//                .subscribe()
        val time = System.currentTimeMillis() - start
        Log.i("setDetail", "time to compute detail: ${time}ms")
    }


    companion object {
        val TAG: String = this::class.java.simpleName

        const val CATEGORY = "c"
        const val QUERY = "q"
        const val PAGE = "p"
        const val AD = "tahiti"

        const val baseUrl = "http://petitesannonces.pf/annonces.php"
        const val searchUrl = "http://petitesannonces.pf/cherche.php"
        const val detailUrl = "https://www.petites-annonces.pf/petiteannonce.php"

        val anchor = Regex("<a [^>]+href=.petiteannonce.php.tahiti=([0-9]+).", RegexOption.IGNORE_CASE)
        val imgre = Regex("<img[^>]+src=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        val titlere = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        val descriptifre = Regex("DESCRIPTIF.*?<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        val pricere = Regex("<p class=.ap.>([0-9,. ]+) XPF / ([0-9,. ]*[0-9])")
        val datere = Regex("Du ([0-9][0-9].[0-9][0-9].[0-9][0-9])")
        val locationre = Regex("LIEU : (.*?)</strong>", RegexOption.IGNORE_CASE)
        val contactre = Regex("INFOS.*?<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)

    }
}

class AdSourceFactory : DataSource.Factory<Int, Ad>() {

    private var filter = ""
    private var category = 9
    private var source: AdSource = create()

    fun filterOn(query: String) {
        filter = query
        source.invalidate()
    }

    fun categorizeOn(query: Int) {
        category = query
        source.invalidate()
    }

    override fun create(): AdSource {
//        Log.d("AdSource", "creation de la source")
        source = PAAdSource(filter, category.toString())
        return source
    }

    fun sourceForAd(ad: Ad): AdSource {
        return source
    }

}

