package pf.miki.matau

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Spanned
import android.text.util.Linkify
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_ad_detail.*
import java.util.*
import java.util.regex.Pattern


fun formatedEuroPrice(ad: Ad): String {
    val pf = Locale.Builder().setLanguage("fr").setRegion("PF").build()
    return if (ad.euroPrice >= 100) String.format(pf, "%.0f €", ad.euroPrice) else String.format(pf, "% .2f €", ad.euroPrice)
}


fun formatedXPFPrice(ad: Ad): String {
    val pf = Locale.Builder().setLanguage("fr").setRegion("PF").build()
    return if (ad.fcpPrice >= 1000000) String.format(pf, "%.2fM XPF", ad.fcpPrice / 1000000f) else String.format(pf, "%,d XPF", ad.fcpPrice)
}

fun fromHtml(text: String): Spanned {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        return Html.fromHtml(text)
    }
}

class AdDetailActivity : AppCompatActivity() {

    val image_root = "http://petitesannonces.pf/"

    companion object {
        val pfPhoneRe = Pattern.compile("(40|87|89)[-0-9. ]+")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_detail)
        imageViewDetailPhoto.setImageResource(R.mipmap.ic_launcher_foregroung)
        val ad: Ad = intent.getParcelableExtra("ad")
        textViewDetailTitle.text = fromHtml(ad.title)
        textViewDetailXPFPrice.text = formatedXPFPrice(ad)
        textViewDetailEuroPrice.text = formatedEuroPrice(ad)
        ad.liveDescription.observe(this, object : Observer<String> {
            override fun onChanged(d: String?) {
                textViewDetailShortDesc.text = fromHtml(d ?: "")
                Linkify.addLinks(textViewDetailShortDesc, Linkify.ALL)
                Linkify.addLinks(textViewDetailShortDesc, pfPhoneRe, "tel:", Linkify.sPhoneNumberMatchFilter,Linkify.sPhoneNumberTransformFilter)            }
        })
        ad.liveLocation.observe(this, object : Observer<String> {
            override fun onChanged(d: String?) {
                textViewDetailLocation.text = fromHtml(d ?: "")
            }
        })
        ad.liveContact.observe(this, object : Observer<String> {
            override fun onChanged(d: String?) {
                textViewDetailContact.text = d ?: ""
                Linkify.addLinks(textViewDetailContact, pfPhoneRe, "tel:", Linkify.sPhoneNumberMatchFilter,Linkify.sPhoneNumberTransformFilter)
            }
        })
        Glide.with(this)
                .load(Uri.parse(imageURL(ad)))
                .into(imageViewDetailPhoto)

        imageViewDetailPhoto.setOnClickListener {
            val detailIntent = Intent(this, ImageActivity::class.java)
            detailIntent.putExtra("image", imageURL(ad))
            startActivity(detailIntent)
        }
    }

    private fun imageURL(ad: Ad) = image_root + ad.vignette.replace("photo/", "photo/b")


}
