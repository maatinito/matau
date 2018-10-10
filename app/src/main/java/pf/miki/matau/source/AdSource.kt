package pf.miki.matau.source

import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import pf.miki.matau.Ad
import pf.miki.matau.BuildConfig
import kotlin.reflect.KFunction2


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

}

/**
 * Enum class to store the list of attribute of an ad.
 * This allows building different configuration for one algorithm taht is able to search on petites-annonces.pf or Big-CE etc...
 * Each enum is able to set a value to an ad
 *
 */
enum class Attribute {
    ID {
        override fun set(ad: Ad, value: List<String>) {
            ad.id = if (value.isNotEmpty()) value[0] else ""
        }
    },
    TITLE {
        override fun set(ad: Ad, value: List<String>) {
            ad.title = if (value.isNotEmpty()) value[0] else ""
        }
    },
    DESCRIPTION {
        override fun set(ad: Ad, value: List<String>) {
            if (value.isNotEmpty()) {
                val d = value.joinToString("\n")
                ad.liveDescription.postValue(d)

                // look for contact in the description to update the contact field
                val contact = ad.liveContact.value ?: ""
                if (contact.isBlank()) {
                    val contacts = contactre.findAll(d)
                            .map(MatchResult::value)
                            .map { s -> s.replace(Regex("[-. ]"), "") }
                            .filter { s -> !contact.contains(s) }
                            .joinToString()
                    if (contacts.isNotEmpty())
                        ad.liveContact.postValue(contacts)
                }
            }
        }
    },
    PRICE {
        override fun set(ad: Ad, value: List<String>) {
            ad.fcpPrice = if (value.isNotEmpty()) value[0].replace(" ", "").replace(',', '.').toInt() else 0
        }
    },
    THUMBNAIL {
        override fun set(ad: Ad, value: List<String>) {
            ad.vignette = if (value.isNotEmpty()) value[0] else ""
//            Log.i("Attribute", "thumbnail=$value")
        }
    },
    DATE {
        override fun set(ad: Ad, value: List<String>) {
            ad.liveDate.postValue(if (value.isNotEmpty()) value[0].replace(" ", "").replace("201", "1") else "")
        }
    },
    CONTACT {
        override fun set(ad: Ad, value: List<String>) {
            ad.liveContact.postValue(if (value.isNotEmpty()) value[0] else "")
        }
    },
    LOCATION {
        override fun set(ad: Ad, value: List<String>) {
            ad.liveLocation.postValue(if (value.isNotEmpty()) value[0] else "")
        }
    },
    IMAGES {
        override fun set(ad: Ad, value: List<String>) {
            ad.liveImages.postValue(value)
            //Log.i("Attribute", "Images=$value")
        }
    };

    abstract fun set(ad: Ad, value: List<String>)

    companion object {
        val contactre = Regex("(40|87|89)[-. ]?[0-9][-. ]?[0-9][-. ]?[0-9][-. ]?[0-9][-. ]?[0-9][-. ]?[0-9]")

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

abstract class AdSource(var filter: String, var category: Category) : PageKeyedDataSource<Int, Ad>() {
    abstract fun setDetail(ad: Ad)
}

abstract class BaseSource(filter: String, category: Category) : AdSource(filter, category) {

    abstract fun getConfiguration(): Configuration
    abstract fun getBaseURL(category: Category, filter: String, pageKey: Int): BaseURL

    private fun loadPage(pageKey: Int, direction: Direction): Pair<MutableList<Ad>, Int?> {
        val baseURL: BaseURL = getBaseURL(category, filter, pageKey)
        val ads = ArrayList<Ad>()
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
                    //Log.i("AdSource", "Looking for attribute ${am.attribute}")
                    val match = am.regex.find(d, startPos)
                    if (match != null) {
                        val value = match.groupValues[1]
                        values.add(value)
                        //Log.i("AdSource", "Attribute ${am.attribute}=$value")
                        startPos = match.range.last
                    } else if (values.isEmpty())
                        Log.i("AdSource", "Attribute ${am.attribute}=No Value!")
                    attributes[am.attribute] = values
                }
                val ad = Ad(conf.source)
                normalizeAttributes(attributes)
                for (am in attributes)
                    am.key.set(ad, am.value)
                ads.add(ad)
                setDetail(ad)
                Log.i("Adsource", "loadAd time=${System.currentTimeMillis() - adStart}ms")
                r1 = r1.next()
            }
            val nextPage: Int? = when (direction) {
                Direction.NEXT -> if (conf.nextRe.containsMatchIn(d)) pageKey + 1 else null
                Direction.PREVIOUS -> if (conf.prevRe.containsMatchIn(d)) pageKey - 1 else null
            }
            Log.i("Adsource", "loadPage time=${System.currentTimeMillis() - start}ms")
            return Pair(ads, nextPage)
        }, { err ->
            Log.e("AdSource", "Unable to load listing of ads $err")
        })
        return Pair(ads, null)
    }

    abstract fun normalizeAttributes(attributes: HashMap<Attribute, MutableList<String>>)

    // this methods loads the page describing an ad to load specific fields not present in the listing of ads like description, images ...

    override fun setDetail(ad: Ad) {
        //Log.i("AdSource", "Requete: ${ad.id}")
        val get = Fuel.get(ad.id)
        val start = System.currentTimeMillis()
        get.responseString { _, _, result ->
            result.fold({ d ->
                var startPos = 0
                val matchers = getConfiguration().detailMatchers
                val attributes = hashMapOf<Attribute, MutableList<String>>()
                for (am in matchers) {
                    val values = ArrayList<String>(2)
                    //Log.i("AdSource", "Looking for attribute ${am.attribute}")
                    do {
                        val match = am.regex.find(d, startPos)
                        if (match != null) {
                            val value = match.groupValues[1]
                            //Log.i("AdSource", "Attribute ${am.attribute}=$value")
                            values.add(value)
                            startPos = match.range.last
                        }
                    } while (match != null)

                    if (values.isNotEmpty())
                        attributes[am.attribute] = values
                    else
                        Log.i("AdSource", "Attribute2 ${ad.id}:${am.attribute}=No Value!")

                }
                normalizeAttributes(attributes)
                for (am in attributes)
                    am.key.set(ad, am.value)
                //Log.i("AdSource", "Final ad=${ad.liveImages.value}")
            }, { err ->
                Log.e("AdSource", "Unable to load ${ad.id} $err")
            })
        }
        Log.i("Adsource", "SetDetail time=${System.currentTimeMillis() - start}ms")
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadInitial: placeHolder=${params.placeholdersEnabled}, size=${params.requestedLoadSize}")
        val (ads, nextKey) = loadPage(1, Direction.NEXT)
        callback.onResult(ads, null, nextKey)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val (ads, nextKey) = loadPage(params.key, Direction.NEXT)
        callback.onResult(ads, nextKey)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Ad>) {
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: placeHolder=${params.key
                ?: "undefined"}, size=${params.requestedLoadSize}")
        val (ads, prevKey) = loadPage(params.key, Direction.PREVIOUS)
        callback.onResult(ads, prevKey)
    }

    companion object {
        val TAG: String = this::class.java.simpleName

    }


}

enum class SourceType(val construct: KFunction2<String, Category, AdSource>) {
    PETITES_ANNONCES(::PAAdSource),
    BIG_CE(::BigCESource);
}

class AdSourceFactory : DataSource.Factory<Int, Ad>() {

    private var filter = ""

    var sourceType: SourceType = SourceType.PETITES_ANNONCES
        set(value) {
            field = value; source.invalidate()
        }

    var category = Category.voiture
        set(value) {
            field = value; source.invalidate()
        }

    fun filterOn(query: String) {
        filter = query
        source.invalidate()
    }

    private var source: AdSource = create()


    override fun create(): AdSource {
        source = sourceType.construct(filter, category)
        return source
    }

    fun sourceForAd(ad: Ad): AdSource {
        return source
    }


}

