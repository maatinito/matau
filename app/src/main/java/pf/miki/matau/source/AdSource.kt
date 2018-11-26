package pf.miki.matau.source

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import pf.miki.matau.BuildConfig
import pf.miki.matau.R
import pf.miki.matau.fragment.ads2.NetworkState
import pf.miki.matau.fragment.ads2.SourceParameters
import pf.miki.matau.repository.AppDatabase
import pf.miki.matau.repository.PAd
import java.lang.RuntimeException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.reflect.KFunction3


enum class Category {
    Vente_appartement,
    vente_maison,
    vente_terrain,
    location_appartement,
    location_maison,
    location_vacances,
    immobilier_autre,
    voiture,
    motos,
    bateau,
    pieces,
    voiture_autre,
    meubles,
    electromenager,
    bricolage,
    informatique,
    jeux,
    multimedia,
    telephone,
    sport,
    vetements,
    puericulture,
    bijoux,
    collection,
    alimentaire,
    bonnes_affaires

}

/**
 * Enum class to store the list of attribute of an ad.
 * This allows building different configuration for one algorithm taht is able to search on petites-annonces.pf or Big-CE etc...
 * Each enum is able to set a value to an ad
 *
 */
enum class Attribute {
    ID {
        override fun set(ad: PAd, value: List<String>) {
            ad.id = if (value.isNotEmpty()) value[0] else ""
        }
    },
    TITLE {
        override fun set(ad: PAd, value: List<String>) {
            ad.title = if (value.isNotEmpty()) value[0] else ""
        }
    },
    DESCRIPTION {
        override fun set(ad: PAd, value: List<String>) {
            if (value.isNotEmpty()) {
                val d = value.joinToString("\n")
                ad.description = d

                // look for contact in the description to update the contact field
                val contact = ad.contact
                if (contact.isBlank()) {
                    val contacts = contactre.findAll(d)
                            .map(MatchResult::value)
                            .map { s -> s.replace(Regex("[-. ]"), "") }
                            .filter { s -> !contact.contains(s) }
                            .joinToString()
                    if (contacts.isNotEmpty())
                        ad.contact = contacts
                }
            }
        }
    },
    PRICE {
        override fun set(ad: PAd, value: List<String>) {
            ad.fcpPrice = if (value.isNotEmpty()) value[0].replace(" ", "").replace(',', '.').toInt() else 0
        }
    },
    THUMBNAIL {
        override fun set(ad: PAd, value: List<String>) {
            ad.vignette = if (value.isNotEmpty()) value[0] else ""
//            Log.i("Attribute", "thumbnail=$value")
        }
    },
    DATE {
        override fun set(ad: PAd, value: List<String>) {
            if (value.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd/MM/yy", Locale.FRENCH)
                val dateString = value[0].replace(" ", "").replace("201", "1")
                try {
                    ad.date = sdf.parse(dateString)
                } catch (e: RuntimeException) {
                    ad.date = Date()
                    Log.e("Matau", "Unable to parse ad date ${value[0]}", e)
                }
            }
        }
    },
    CONTACT {
        override fun set(ad: PAd, value: List<String>) {
            ad.contact = if (value.isNotEmpty()) value[0] else ""
        }
    },
    LOCATION {
        override fun set(ad: PAd, value: List<String>) {
            ad.location = if (value.isNotEmpty()) value[0] else ""
        }
    },
    IMAGES {
        override fun set(ad: PAd, value: List<String>) {
            ad.imageList = value
        }
    };

    abstract fun set(ad: PAd, value: List<String>)

    companion object {
        val contactre = Regex("(\\+?689[-. ]?)?(40|87|89)[-. ]?[0-9][-. ]?[0-9][-. ]?[0-9][-. ]?[0-9][-. ]?[0-9][-. ]?[0-9]")

    }

}

// to determine if the loading of a page must look for a previous or next page

enum class Direction {
    NEXT,
    PREVIOUS
}


data class AttributeMatcher(val attribute: Attribute, val regex: Regex)

data class Configuration(val source: String,
                         val anchor: Regex,
                         val matchers: List<AttributeMatcher>,
                         val nextRe: Regex,
                         val prevRe: Regex,
                         val detailMatchers: List<AttributeMatcher>)

data class BaseURL(val url: String, val params: List<Pair<String, String>>)

abstract class AdSource(var filter: String, var category: Category) : PageKeyedDataSource<Int, PAd>() {
    abstract fun setDetail(ads: ArrayList<PAd>)

    val networkState = MutableLiveData<NetworkState>()
    val refreshState = MutableLiveData<NetworkState>()
    val updatedAds = MutableLiveData<List<PAd>>()
}

abstract class BaseSource(filter: String, category: Category, val context: Context) : AdSource(filter, category) {
    private val executor: Executor = Executors.newFixedThreadPool(1)

    abstract fun getConfiguration(): Configuration
    abstract fun getBaseURL(category: Category, filter: String, pageKey: Int): BaseURL

    private fun loadPage(pageKey: Int, direction: Direction): Pair<MutableList<PAd>, Int?> {
        networkState.postValue(NetworkState.LOADING)
        val baseURL: BaseURL = getBaseURL(category, filter, pageKey)
        val ads = ArrayList<PAd>()

        val get = Fuel.get(baseURL.url, baseURL.params)
//        Log.i("AdSource", "Requete: ${get.url}")
        val (_, _, result) = get.responseString()
        result.fold({ d ->
            val start = System.currentTimeMillis()
            val conf = getConfiguration()
            val matchers = conf.matchers
            val a = conf.anchor
            var r1 = a.find(d)
            while (r1 != null) {
                var startPos = r1.range.first
                val adStart = System.currentTimeMillis()
                val attributes = hashMapOf<Attribute, MutableList<String>>()
                for (am in matchers) {
                    val values = ArrayList<String>(2)
//                    Log.i("Matau.AdSource", "Looking for attribute ${am.attribute}")
                    val match = am.regex.find(d, startPos)
                    if (match != null) {
                        val value = match.groupValues[1]
                        values.add(value)
//                        Log.i("Matau.AdSource", "Attribute ${am.attribute}=$value")
                        startPos = match.range.last
                    } else if (values.isEmpty())
                        Log.i("Matau.AdSource", "Attribute ${am.attribute}=No Value!")
                    attributes[am.attribute] = values
                }
                val ad = PAd(conf.source)
                normalizeAttributes(attributes)
                for (am in attributes)
                    am.key.set(ad, am.value)
                ads.add(ad)
//                Log.i("Adsource", "loadAd time=${System.currentTimeMillis() - adStart}ms")
                r1 = r1.next()
            }
            val nextPage: Int? = when (direction) {
                Direction.NEXT -> if (conf.nextRe.containsMatchIn(d)) pageKey + 1 else null
                Direction.PREVIOUS -> if (conf.prevRe.containsMatchIn(d)) pageKey - 1 else null
            }
//            Log.i("Adsource", "loadPage time=${System.currentTimeMillis() - start}ms")
            executor.execute { setDetail(ads) }
            networkState.postValue(NetworkState.LOADED)
            return Pair(ads, nextPage)
        }, { err ->
            networkState.postValue(NetworkState.error(context.resources.getString(R.string.network_error, err.message)))
            Log.e("AdSource", "Unable to load listing of ads $err")
        })
        return Pair(ads, null)
    }

    abstract fun normalizeAttributes(attributes: HashMap<Attribute, MutableList<String>>)

    // this methods loads the page describing an ad to load specific fields not present in the listing of ads like description, images ...

    override fun setDetail(ads: ArrayList<PAd>) {
        var start = System.currentTimeMillis()
        val db = AppDatabase.getDatabase(context)
        val adIds = ads.map { it.id }
        val cachedAds = db.pAdDao().loadAds(adIds).associateBy { it.id }
//        Log.i("Adsource", "SetDetail dbaccess=${System.currentTimeMillis() - start}ms ${Thread.currentThread().name}")
        start = System.currentTimeMillis()
        ads.forEach { ad ->
            val cachedAd = cachedAds[ad.id]
            if (cachedAd != null) {
                fillAttributes(ad, cachedAd)
//                Log.i("Matau.Source", "PAd already cached: ${ad.title}")
                return@forEach
            }
            // PAd not in cache ==> get the detail from the web
            val get = Fuel.get(ad.id)
            val (_, _, result) = get.responseString()
            result.fold({ d ->
                var startPos = 0
                val matchers = getConfiguration().detailMatchers
                val attributes = hashMapOf<Attribute, MutableList<String>>()
                for (am in matchers) {
                    val values = ArrayList<String>(2)
//                    Log.i("AdSource", "Looking for attribute ${am.attribute}")
                    do {
                        val match = am.regex.find(d, startPos)
                        if (match != null) {
                            val value = match.groupValues[1]
//                            Log.i("AdSource", "Attribute ${am.attribute}=$value")
                            values.add(value)
                            startPos = match.range.last
                        }
                    } while (match != null)

                    if (values.isNotEmpty())
                        attributes[am.attribute] = values
                    else
                        Log.i("AdSource", "Attribute2 ${ad.id}:${am.attribute}=No Value!")

                }
                // normalize and sets the attributes
                normalizeAttributes(attributes)
                for (am in attributes)
                    am.key.set(ad, am.value)
                // store the ad in database to serve as a cache
                db.pAdDao().insert(ad)
            }, { err ->
                Log.e("AdSource", "Unable to load ${ad.id} $err")
            })
        }
        updatedAds.postValue(ads)
//        Log.i("Adsource", "SetDetail time=${System.currentTimeMillis() - start}ms, cached ads count = ${cachedAds.size}")
    }

    private fun fillAttributes(ad: PAd, cachedAd: PAd) {
        with(ad) {
            pinned = cachedAd.pinned
            images = cachedAd.images
            description = cachedAd.description
            location = cachedAd.location
            contact = cachedAd.contact
            date = cachedAd.date
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, PAd>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadInitial: placeHolder=${params.placeholdersEnabled}, size=${params.requestedLoadSize}")
        val (ads, nextKey) = loadPage(1, Direction.NEXT)
        callback.onResult(ads, null, nextKey)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PAd>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val (ads, nextKey) = loadPage(params.key, Direction.NEXT)
        callback.onResult(ads, nextKey)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PAd>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val (ads, prevKey) = loadPage(params.key, Direction.PREVIOUS)
        callback.onResult(ads, prevKey)
    }

    companion object {
        val TAG: String = this::class.java.simpleName

    }


}

enum class SourceType(val construct: KFunction3<String, Category, Context, AdSource>) {
    PETITES_ANNONCES(::PAAdSource),
    BIG_CE(::BigCESource);
}

class AdSourceFactory(val context: Context) : DataSource.Factory<Int, PAd>() {

    var source = MutableLiveData<AdSource>()

    override fun create(): AdSource {
        val sp = liveParameters.value
                ?: SourceParameters(SourceType.PETITES_ANNONCES, Category.voiture, "")
        with(sp) {
            val s = sourceType.construct(search, category, context)
            source.postValue(s)
            return s
        }
    }

    var liveParameters = MutableLiveData<SourceParameters>()

    init {
        liveParameters.observeForever {
            source.value?.invalidate()
        }
    }

    var category: Category
        get() = liveParameters.value?.category ?: Category.voiture
        set(value) {
            if (value != liveParameters.value?.category)
                liveParameters.value = liveParameters.value?.new(value) ?: SourceParameters(SourceType.PETITES_ANNONCES, value, "")
        }

    var sourceType
        get() = liveParameters.value?.sourceType ?: SourceType.PETITES_ANNONCES
        set(value) {
            if (value != liveParameters.value?.sourceType)
                liveParameters.value = liveParameters.value?.new(value) ?: SourceParameters(value, Category.voiture, "")
        }


    var search
        get() = liveParameters.value?.search ?: ""
        set(value) {
            if (value != liveParameters.value?.search)
                liveParameters.value = liveParameters.value?.new(value) ?: SourceParameters(SourceType.PETITES_ANNONCES, Category.voiture, value)
        }

}
