package pf.miki.matau.ViewModel

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.paging.PagedListAdapter
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_ad_detail.*
import kotlinx.android.synthetic.main.announce_layout.view.*
import pf.miki.matau.Ad
import pf.miki.matau.R
import pf.miki.matau.formatedXPFPrice
import pf.miki.matau.fromHtml

class AdPagedAdapter(val owner : LifecycleOwner, val clickListener: (Ad) -> Unit) : PagedListAdapter<Ad, AdPagedAdapter.ViewHolder>(Ad.EqualCallBack) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.announce_layout, parent, false)
        return ViewHolder(owner,v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ad : Ad? = getItem(position)
        if (ad != null) holder.bind(ad,clickListener)
    }

    //the class is hodling the list view
    class ViewHolder(val owner: LifecycleOwner, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image_root = "http://petitesannonces.pf/"

        fun bind(ad: Ad, clickListener: (Ad) -> Unit) {
            itemView.textViewTitle.text = fromHtml(ad.title)
            itemView.textViewPrice.text = formatedXPFPrice(ad)
//            itemView.textViewLocation.text = fromHtml(ad.location)
            Glide.with(itemView.context)
                    .load(Uri.parse(image_root + ad.vignette))
                    .into(itemView.imageView)

            itemView.setOnClickListener { clickListener(ad) }
            ad.liveDescription.observe(owner,object : Observer<String> {
                override fun onChanged(d: String?) {
                    val s = fromHtml(if (d != null) d.substring(0,Math.min(40, d.length)) else "")
                    itemView.textViewShortDesc.text = s
                }
            })
            ad.liveLocation.observe(owner,object : Observer<String> {
                override fun onChanged(d: String?) {
                    itemView.textViewLocation.text  = fromHtml(d ?: "")
                }
            })
        }
    }

}
