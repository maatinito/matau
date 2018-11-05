package pf.miki.matau

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Spanned
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_ad_detail.*
import pf.miki.matau.fragment.BaseAdViewModel
import pf.miki.matau.fragment.ads.AdViewModel
import pf.miki.matau.repository.AppDatabase
import pf.miki.matau.source.Attribute
import pf.miki.matau.repository.Ad
import pf.miki.matau.repository.PAd
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


fun formatedEuroPrice(euroPrice: Float): String {
    val pf = Locale.Builder().setLanguage("fr").setRegion("PF").build()
    return if (euroPrice >= 100) String.format(pf, "%.0f €", euroPrice) else String.format(pf, "% .2f €", euroPrice)
}


fun formatedXPFPrice(fcpPrice: Int): String {
    val pf = Locale.Builder().setLanguage("fr").setRegion("PF").build()
    return if (fcpPrice >= 1000000) String.format(pf, "%.2fM XPF", fcpPrice / 1000000f) else String.format(pf, "%,d XPF", fcpPrice)
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

    val format = DateFormat.getDateFormat(this)

    companion object {
        val pfPhoneRe = Pattern.compile(Attribute.contactre.pattern)
    }


    private lateinit var adViewModel: BaseAdViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_detail)

        adViewModel = ViewModelProviders.of(this).get(BaseAdViewModel::class.java)

        val ad_id = intent.getStringExtra("ad")

        val liveAd: LiveData<PAd> = adViewModel.loadAd(ad_id)
        liveAd.observe(this, Observer<PAd> { ad ->
            if (ad != null) {
                // prices
                textViewDetailTitle.text = fromHtml(ad.title)
                textViewDetailXPFPrice.text = formatedXPFPrice(ad.fcpPrice)
                textViewDetailEuroPrice.text = formatedEuroPrice(ad.euroPrice)
                // View on site link
                val linkText = resources.getString(R.string.viewOnSite, ad.id)
                textViewDetailId.text = fromHtml(linkText)
                textViewDetailId.movementMethod = LinkMovementMethod.getInstance()
                // description
                textViewDetailShortDesc.text = fromHtml(ad.description)
                Linkify.addLinks(textViewDetailShortDesc, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
                Linkify.addLinks(textViewDetailShortDesc, pfPhoneRe, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter)
                // location
                textViewDetailLocation.text = fromHtml(ad.location)
                // contact
                textViewDetailContact.text = ad.contact
                Linkify.addLinks(textViewDetailContact, pfPhoneRe, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter)
                // date
                textViewDetailDate.text = format.format(ad.date)
                val images = ad.imageList
                if (images.isNotEmpty()) {
                    Glide.with(this@AdDetailActivity)
                            .load(Uri.parse(images[0]))
                            .into(imageViewDetailPhoto)
                    imageViewDetailPhoto.setOnClickListener {
                        val detailIntent = Intent(this@AdDetailActivity, ImageActivity::class.java)
                        detailIntent.putExtra("image", images[0])
                        startActivity(detailIntent)
                    }
                }
                saveToggleButton.setOnCheckedChangeListener(null)
                saveToggleButton.isChecked = ad.pinned
                saveToggleButton.setOnCheckedChangeListener { _, isChecked -> adViewModel.pin(ad, isChecked) }
            }
        })
    }
}