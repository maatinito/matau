package pf.miki.matau.fragment.pin

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.staggered_ad_fragment.*
import pf.miki.matau.AdDetailActivity
import pf.miki.matau.R
import pf.miki.matau.fragment.BaseAdFragment
import pf.miki.matau.fragment.StaggeredAdAdapter
import pf.miki.matau.repository.PAd
import pf.miki.matau.repository.toAd

/**
 * displays pinned ads.
 * Interaction is handled in BaseAdFragment wich transmits the AdInteractionListerner to the adapter
 * The ViewModel is linked to the database to read the pinned ads
 * Whenever the database is updated, room triggers an evetn through the LiveData list of ads and the new list is transferded to the adapter
 */
class PinnedAdFragment : BaseAdFragment() {

    companion object {
        fun newInstance() = PinnedAdFragment()
    }

    private lateinit var viewModel: PinnedAdViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.staggered_ad_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the current list of pinned ads
        viewModel = ViewModelProviders.of(this).get(PinnedAdViewModel::class.java)
        // choose a Staggered layout to display the ads
        pinnedAdList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // gives the RecyclerView the adapter responsible to give  ads and display them
        pinnedAdList.adapter = adapter
        // Whenever the database is updated, room triggers an evetn through the LiveData list of ads and the new list is transferded to the adapter
        viewModel.allAds.observe(this, Observer { adapter.submitList(it) })
    }

//
//    private fun adClicked(pAd: PAd) {
//        Toast.makeText(this.activity, "Ad clicked ${pAd.title}", Toast.LENGTH_SHORT)
//        val detailIntent = Intent(this.activity, AdDetailActivity::class.java)
//        detailIntent.putExtra("ad", pAd.toAd())
//        startActivity(detailIntent)
//    }
//
//    fun pin(ad : PAd, isChecked : Boolean)  = viewModel.pin(ad,isChecked)

}
