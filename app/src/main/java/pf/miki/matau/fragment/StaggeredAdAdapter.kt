package pf.miki.matau.fragment

import android.arch.paging.PagedListAdapter
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.staggered_card_layout.view.*
import pf.miki.matau.helpers.AdInteractionListener
import pf.miki.matau.R
import pf.miki.matau.formatedXPFPrice
import pf.miki.matau.fromHtml
import pf.miki.matau.repository.PAd
import java.util.*


class StaggeredAdAdapter : PagedListAdapter<PAd, StaggeredAdAdapter.ViewHolder>(PAd.EqualCallBack) {

    var listener : AdInteractionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.staggered_card_layout, parent, false)
        return ViewHolder(listener,v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ad: PAd? = getItem(position)
        if (ad != null)
            holder.bind(ad)
    }

    /**
     * class to display the card.
     * the listener is called whenever the pin button or the whole card is pressed
      */

    class ViewHolder(val listener : AdInteractionListener?, iv: View) : RecyclerView.ViewHolder(iv) {

        fun bind(ad: PAd) {
            itemView.pinnedTitleTextView.text = fromHtml(ad.title)
            itemView.pinnedPriceTextView.text = formatedXPFPrice(ad.fcpPrice)
            itemView.pinnedPinButton.setOnCheckedChangeListener(null)
            itemView.pinnedPinButton.isChecked = ad.pinned

            // transfer the pin check to my fragement owner
            if (listener != null) {
                itemView.pinnedPinButton.setOnCheckedChangeListener { _, isChecked -> listener.adPinned(ad, isChecked) }
                itemView.setOnClickListener { listener.adSelected(ad) }
            }
            val myOptions = RequestOptions()
                    .placeholder(R.mipmap.ic_launcher_foregroung)
                    .error(R.mipmap.ic_launcher_foregroung)
//                    .transforms(RoundedCorners(20))

            Glide.with(itemView.context)
                    .load(Uri.parse(ad.vignette))
                    .apply(myOptions)
                    .into(itemView.pinnedImageView)

            val difference : Int = ((Date().time - ad.date.time) / 86400000).toInt()
            itemView.pinnedLayout.setBackgroundColor(Color.argb(Math.min(difference, 125), 0x83, 0x3b, 0x14))
        }
    }

}