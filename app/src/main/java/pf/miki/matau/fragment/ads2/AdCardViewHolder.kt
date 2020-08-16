package pf.miki.matau.fragment.ads2

import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import pf.miki.matau.R
import pf.miki.matau.formatedXPFPrice
import pf.miki.matau.fromHtml
import pf.miki.matau.helpers.AdInteractionListener
import pf.miki.matau.repository.PAd
import java.util.*

class AdCardViewHolder(itemView: View, val listener: AdInteractionListener?) : RecyclerView.ViewHolder(itemView) {

    private val title: TextView = itemView.findViewById(R.id.textViewTitle)
    private val price: TextView = itemView.findViewById(R.id.textViewPrice)
    private val desc: TextView = itemView.findViewById(R.id.textViewShortDesc)
    private val location: TextView = itemView.findViewById(R.id.textViewLocation)
    private val pin: ToggleButton = itemView.findViewById(R.id.toggleButtonPin)
    private val thumbnail: ImageView = itemView.findViewById(R.id.imageView)

    fun bind(ad: PAd?) {
        if (ad == null)
            return
        title.text = fromHtml(ad.title)
        price.text = formatedXPFPrice(ad.fcpPrice)
        desc.text = fromHtml(ad.description.substring(0, Math.min(40, ad.description.length)))
        location.text = fromHtml(ad.location)
        pin.setOnCheckedChangeListener(null)
        pin.isChecked = ad.pinned
        Log.i("Adapter", "isSelected=${ad.pinned}")

        // transfer the pin check to my owner
        if (listener != null) {
            pin.setOnCheckedChangeListener { _, isChecked -> listener.adPinned(ad, isChecked) }
            itemView.setOnClickListener { listener.adSelected(ad) }
        }
        val myOptions = RequestOptions()
                .placeholder(R.mipmap.ic_launcher_foregroung)
                .error(R.mipmap.ic_launcher_foregroung)

        if (ad.vignette.startsWith("http"))
            Glide.with(itemView.context)
                    .load(Uri.parse(ad.vignette))
                    .apply(myOptions)
                    .into(thumbnail)
        else
            Glide.with(itemView.context)
                    .load(R.mipmap.ic_launcher_foregroung)
                    .apply(myOptions)
                    .into(thumbnail)

        val difference: Int = ((Date().time - ad.date.time) / 86400000).toInt()
        itemView.setBackgroundColor(Color.argb(Math.min(difference, 125), 0x83, 0x3b, 0x14))
    }

    companion object {
        fun create(parent: ViewGroup, listener: AdInteractionListener?): AdCardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.announce_layout, parent, false)
            return AdCardViewHolder(view, listener)
        }

    }
}