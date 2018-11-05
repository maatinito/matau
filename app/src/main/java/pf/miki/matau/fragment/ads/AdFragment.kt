package pf.miki.matau.fragment.ads

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ad_fragment.*
import pf.miki.matau.AdDetailActivity
import pf.miki.matau.R
import pf.miki.matau.repository.Ad
import pf.miki.matau.repository.PAd
import pf.miki.matau.repository.toAd
import pf.miki.matau.source.Category

class AdFragment : Fragment() {

    val adapter = AdPagedAdapter(this) { adClicked(it) }

    companion object {
        fun newInstance() = AdFragment()
    }

    private lateinit var viewModel: AdViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ad_fragment, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // build a ViewModel to maintain the data
        // whenever the list of ads change ==> submit the list to the adapter
        viewModel = ViewModelProviders.of(this).get(AdViewModel::class.java)
        viewModel.category = Category.voiture
        viewModel.liveAds.observe(this, Observer<PagedList<PAd>> { ads -> adapter.submitList(ads) })

        // RecyclerView setup: setup a linear layout & gives the adapter which gets data from the PagedList<Ad>
        adList.layoutManager = LinearLayoutManager(this.context,LinearLayout.VERTICAL,false)
        adList.adapter = adapter
    }

    private fun adClicked(ad: PAd) {
        val detailIntent = Intent(this.activity, AdDetailActivity::class.java)
        detailIntent.putExtra("ad", ad.toAd()) // TODO: refactor detail activity
        startActivity(detailIntent)
    }

    fun pin(ad: PAd, isChecked: Boolean)  = viewModel.pin(ad,isChecked)

}
