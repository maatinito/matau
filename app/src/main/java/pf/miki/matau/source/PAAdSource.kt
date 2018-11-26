package pf.miki.matau.source

import android.content.Context
import android.util.Log
import java.text.Normalizer
import java.util.function.Predicate

class PAAdSource(filter: String, category: Category, context: Context) : BaseSource(filter, category, context) {
    override fun normalizeAttributes(attributes: HashMap<Attribute, MutableList<String>>) {
        attributes.forEach {
            when (it.key) {
                Attribute.ID -> it.value.forEachIndexed { i, s -> it.value[i] = detailUrl + s }
//                Attribute.IMAGES -> it.value.forEachIndexed { i, s -> it.value[i] = detailUrl + s }
                Attribute.THUMBNAIL,
                Attribute.IMAGES -> {
                    it.value.forEachIndexed { i, s ->
                        if (!s.startsWith("photo"))
                            it.value.removeAt(i);
                    }
                    it.value.forEachIndexed { i, s -> it.value[i] = detailUrl + s }
                }
                else -> {
                }
            }
        }
    }

    fun stripAccents(s: String): String {
        val string = Normalizer.normalize(s, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(string, "")
    }

    override fun getBaseURL(category: Category, filter: String, pageKey: Int): BaseURL {
        val url = if (filter.isEmpty()) baseUrl else searchUrl
        val params = mutableListOf<Pair<String, String>>()
        params.add(CATEGORY to (category2Int.get(category)?.toString() ?: "9"))
        if (filter.isNotEmpty())
            params.add(QUERY to stripAccents(filter))
        params.add(PAGE to pageKey.toString())
        return BaseURL(url, params)
    }

    override fun getConfiguration(): Configuration = configuration

    companion object {

        const val CATEGORY = "c"
        const val QUERY = "q"
        const val PAGE = "p"
        const val AD = "tahiti"

        const val baseUrl = "http://petites-annonces.pf/annonces.php"
        const val searchUrl = "http://petites-annonces.pf/cherche.php"
        const val detailUrl = "https://www.petites-annonces.pf/"

        val anchor = Regex("<a [^>]*?href=.petiteannonce.php.tahiti", RegexOption.IGNORE_CASE)
        private val idre = Regex("<a.*?href=.(.*?tahiti=[0-9]+)")
        private val imgre = Regex("<img[^>]+src=\"((photo|themes/blue/img/no_img|themes/blue/img/vig).*?)\"", RegexOption.IGNORE_CASE)
        private val titlere = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        private val descriptifre = Regex("DESCRIPTIF.*?<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        private val pricere = Regex("<p class=.ap.>([0-9,. ]+) XPF / ([0-9,. ]*[0-9])")
        private val datere = Regex("Du ([0-9][0-9].[0-9][0-9].[0-9][0-9])")
        private val locationre = Regex("LIEU : (.*?)</strong>", RegexOption.IGNORE_CASE)
        private val contactre = Regex("INFOS.*?<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        private val nextpagere = Regex("<a [^>]*?p=([0-9]+)[^>]*?>&raquo;</a>", RegexOption.IGNORE_CASE)
        private val prevpagere = Regex("<a [^>]*?p=([0-9]+)[^>]*?>&laquo;</a>", RegexOption.IGNORE_CASE)

        val category2Int = hashMapOf<Category, Int>(
                Category.Vente_appartement to 1,
                Category.vente_maison to 2,
                Category.vente_terrain to 3,
                Category.location_appartement to 4,
                Category.location_maison to 5,
                Category.location_vacances to 6,
                Category.immobilier_autre to 7,
                Category.voiture to 9,
                Category.motos to 10,
                Category.bateau to 11,
                Category.pieces to 13,
                Category.voiture_autre to 14,
                Category.meubles to 15,
                Category.electromenager to 15,
                Category.bricolage to 16,
                Category.informatique to 17,
                Category.jeux to 18,
                Category.multimedia to 19,
                Category.telephone to 20,
                Category.sport to 21,
                Category.vetements to 23,
                Category.puericulture to 51,
                Category.bijoux to 24,
                Category.collection to 25,
                Category.bonnes_affaires to 26,
                Category.alimentaire to 52
        )


        private val configuration = Configuration(
                "petites-annonces",
                anchor,
                listOf(
                        AttributeMatcher(Attribute.ID, idre),
                        AttributeMatcher(Attribute.THUMBNAIL, imgre),
                        AttributeMatcher(Attribute.TITLE, titlere),
                        AttributeMatcher(Attribute.PRICE, pricere)),
                nextpagere,
                prevpagere,
                listOf(AttributeMatcher(Attribute.DATE, datere),
                        AttributeMatcher(Attribute.LOCATION, locationre),
                        AttributeMatcher(Attribute.IMAGES, imgre),
                        AttributeMatcher(Attribute.DESCRIPTION, descriptifre),
                        AttributeMatcher(Attribute.CONTACT, contactre)))
    }
}