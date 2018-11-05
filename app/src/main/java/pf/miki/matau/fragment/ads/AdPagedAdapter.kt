package pf.miki.matau.fragment.ads

import android.arch.paging.PagedListAdapter
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.announce_layout.view.*
import pf.miki.matau.R
import pf.miki.matau.formatedXPFPrice
import pf.miki.matau.fromHtml
import pf.miki.matau.repository.PAd

class AdPagedAdapter(val owner: AdFragment, val clickListener: (PAd) -> Unit) : PagedListAdapter<PAd, AdPagedAdapter.ViewHolder>(PAd.EqualCallBack) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.announce_layout, parent, false)
        return ViewHolder(owner, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ad: PAd? = getItem(position)
        if (ad != null) holder.bind(ad, clickListener)
    }

    //the class is hodling the list view
    class ViewHolder(val owner: AdFragment, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image_root = "http://petitesannonces.pf/"

        fun bind(ad: PAd, clickListener: (PAd) -> Unit) {
            itemView.textViewTitle.text = fromHtml(ad.title)
            itemView.textViewPrice.text = formatedXPFPrice(ad.fcpPrice)
            Glide.with(itemView.context)
                    .load(Uri.parse(ad.vignette))
                    .into(itemView.imageView)

            // default click to open detail view
            itemView.setOnClickListener { clickListener(ad) }

            // transfer the pin check to my fragement owner
            itemView.toggleButtonPin.setOnCheckedChangeListener { _, isChecked -> owner.pin(ad,isChecked) }
            itemView.toggleButtonPin.isChecked = ad.pinned

//            ad.liveDescription.observe(owner, object : Observer<String> {
//                override fun onChanged(d: String?) {
//                    val s = fromHtml(if (d != null) d.substring(0, Math.min(40, d.length)) else "")
//                    itemView.textViewShortDesc.text = s
//                }
//            })
//
//            ad.liveLocation.observe(owner, object : Observer<String> {
//                override fun onChanged(d: String?) {
//                    itemView.textViewLocation.text = fromHtml(d ?: "")
//                }
//            })
//            ad.liveDate.observe(owner, object : Observer<Date> {
//                override fun onChanged(date: Date?) {
//                    if (date != null) {
//                        val dateEnd = Date()
//                        val difference = Math.round((dateEnd.time - date.time) / 86400000f)
//                      Log.i("CardView", "difference = ${difference} from ${dateStart} ${Math.min(difference, 125)}")
//                        itemView.cardViewLayout.setBackgroundColor(Color.argb(Math.min(difference, 125), 0x83, 0x3b, 0x14))
//                    }
//                }
//            })
        }
    }

}
