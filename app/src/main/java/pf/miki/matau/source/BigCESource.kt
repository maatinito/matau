package pf.miki.matau.source

class BigCESource(filter: String, category: Category) : BaseSource(filter, category) {

    override fun getBaseURL(category: Category, filter: String, pageKey: Int): BaseURL {
        val params = mutableListOf<Pair<String, String>>()
        params.add(CATEGORY to (category2Int[category]?.toString() ?: "0"))
        if (filter.isNotEmpty())
            params.add(TITLE to filter)
        params.add("sort_by" to "created")
        params.add("sort_order" to "DESC")
        params.add(PAGE to pageKey.toString())
        return BaseURL(baseUrl, params)
    }

    override fun getConfiguration(): Configuration = configuration

    override fun normalizeAttributes(attributes: HashMap<Attribute, MutableList<String>>) {
        attributes.forEach {
            when (it.key) {
                Attribute.ID -> it.value.forEachIndexed { i, s -> it.value[i] = imageUrl + s }
                Attribute.IMAGES -> it.value.forEachIndexed { i, s -> it.value[i] = imageUrl + s }
                Attribute.THUMBNAIL -> it.value.forEachIndexed { i, s -> it.value[i] = imageUrl + s }
                Attribute.DATE -> it.value.forEachIndexed { i, s -> it.value[i] = s.replace(" ", "").replace("201", "1") }
            }
        }
    }

    companion object {

        const val CATEGORY = "field_categories_annonce_target_id"
        const val TITLE = "title"
        const val PAGE = "p"

        const val baseUrl = "https://www.big-ce.pf/les-petites-annonces"
        const val detailUrl = "https://www.big-ce.pf/les-petites-annonces/"
        const val imageUrl = "https://www.big-ce.pf"

        // regexp for listing
        val anchor = Regex("<div class=.m-annonce-listing.>", RegexOption.IGNORE_CASE)
        private val idre = Regex("<a href=.(/les-petites-annonces/.*?)\"")
        private val thumbnailre = Regex("<img[^>]+src=\"(.*?)\"", RegexOption.IGNORE_CASE)
        private val datere = Regex("> ([0-9][0-9] / [0-9][0-9] / [0-9][0-9][0-9][0-9])")
        private val titlere = Regex("<a href=.*?>(.*?)</a>", RegexOption.DOT_MATCHES_ALL)
        private val pricere = Regex("([0-9,. ]+) FCP")
        /*<div class="field field--name-field-images field--type-image field--label-hidden item">
                <a href="https://www.big-ce.pf/sites/default/files/2018-10/FB_IMG_1538069892928.jpg"
                   title="Peugeot Partner - Diesel  - Utilitaire"
                   data-colorbox-gallery="gallery-annonce-8345-b8XNzKmsQi4"
                   class="colorbox
                   cboxElement"
                   data-cbox-img-attrs="{&quot;alt&quot;:&quot;&quot;}">
                   <img src="/sites/default/files/styles/colorbox_custom/public/2018-10/FB_IMG_1538069892928.jpg?itok=SA93J566"
                   alt=""
                   typeof="foaf:Image"
                   class="img-responsive" width="1110" height="540">

        </a>

            </div>*/
        // regexp for detail
        private val descriptifre = Regex("<div class=..*?field--name-body.*?>(.*?)</div>", RegexOption.DOT_MATCHES_ALL)
        private val imgre = Regex("field--name-field-images.*?<img src=\"(.*?)\"", RegexOption.DOT_MATCHES_ALL)

        private val nextpagere = Regex("title=.Aller à la page suivante.", RegexOption.IGNORE_CASE)
        private val prevpagere = Regex("title=.Aller à la page précédente.", RegexOption.IGNORE_CASE)


        val category2Int = hashMapOf(
                Category.Vente_appartement to 175,
                Category.vente_maison to 175,
                Category.vente_terrain to 175,
                Category.location_appartement to 182,
                Category.location_maison to 182,
                Category.location_vacances to 202,
                Category.immobilier_autre to 0,
                Category.voiture to 188,
                Category.motos to 187,
                Category.bateau to 194,
                Category.pieces to 0,
                Category.voiture_autre to 0,
                Category.meubles to 185,
                Category.electromenager to 193,
                Category.bricolage to 179,
                Category.informatique to 189,
                Category.jeux to 197,
                Category.multimedia to 184,
                Category.telephone to 196,
                Category.sport to 195,
                Category.vetements to 177,
                Category.puericulture to 192,
                Category.bijoux to 201,
                Category.collection to 190,
                Category.alimentaire to 0
        )
        private val configuration = Configuration(
                "petites-annonces",
                anchor,
                listOf(
                        AttributeMatcher(Attribute.ID, idre),
                        AttributeMatcher(Attribute.THUMBNAIL, thumbnailre),
                        AttributeMatcher(Attribute.DATE, datere),
                        AttributeMatcher(Attribute.TITLE, titlere),
                        AttributeMatcher(Attribute.PRICE, pricere)),
                nextpagere,
                prevpagere,
                listOf(
                        AttributeMatcher(Attribute.DESCRIPTION, descriptifre),
                        AttributeMatcher(Attribute.IMAGES, imgre)))
    }
}