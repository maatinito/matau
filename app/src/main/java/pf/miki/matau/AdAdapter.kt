package pf.miki.matau

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.graphics.ColorSpace.match
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.system.Os.accept
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.announce_layout.view.*

class AdAdapter(val owner: LifecycleOwner, val clickListener: (Ad) -> Unit) : RecyclerView.Adapter<AdAdapter.ViewHolder>() {
    val raw = ArrayList<Ad>()
    var filtered : MutableList<Ad> = ArrayList<Ad>()
    var filter : Regex = Regex("");


    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.announce_layout, parent, false)
        return ViewHolder(owner,v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: AdAdapter.ViewHolder, position: Int) {
        holder.bind(filtered[position],clickListener)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return filtered.size
    }

    fun filterOn(query: String?) {
        if (filtered.equals(query))
            return
        filtered.clear()
        filter = if (query == null) Regex("") else Regex(Regex.escape(query),RegexOption.IGNORE_CASE);
        if (filter.pattern.length == 0) {
            filtered.addAll(raw)
        } else {
            filtered.addAll(raw.filter { item_matches(it,filter) })
        }
        notifyDataSetChanged()
    }

    //the class is hodling the list view
    class ViewHolder(val owner: LifecycleOwner, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image_root = "http://petitesannonces.pf/"

        fun bind(ad: Ad, clickListener: (Ad) -> Unit) {
            itemView.textViewTitle.text = fromHtml(ad.title)
            itemView.textViewPrice.text = ad.fcpPrice.toString() + " XPF"
//            itemView.textViewLocation.text = fromHtml(ad.location)
            Glide.with(itemView.context)
                    .load(Uri.parse(image_root + ad.vignette))
                    .into(itemView.imageView)

            itemView.setOnClickListener { clickListener(ad) }
            ad.liveDescription.observe(owner,object : Observer<String> {
                override fun onChanged(d: String?) {
                    val s =fromHtml(if (d != null) d.substring(0,Math.min(40, d.length)) else "")
                    itemView.textViewShortDesc.text = s
                }
            })
        }

        fun fromHtml(text : String) : Spanned {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                return Html.fromHtml(text)
            }
        }

    }

    fun removeAllItems() {
        raw.clear()
        filtered.clear()
        notifyDataSetChanged()
    }

    fun add(item: Ad) {
        raw.add(item)
        if (item_matches(item,filter)) {
            filtered.add(item)
            notifyItemInserted(filtered.count())
        }
    }

    private fun item_matches(item: Ad, filter: Regex): Boolean {

        return item.title.contains(filter)
    }

}