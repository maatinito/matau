package pf.miki.matau.fragment.history

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.staggered_ad_fragment.*

import pf.miki.matau.R
import pf.miki.matau.fragment.BaseAdFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization liveParameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * History fragment displaying ads visited by the user
 * The container listen to ad interaction like ad clicked or pinned via the AdInteractionListener transmitted to the adapter
 *
 */
class HistoryFragment : BaseAdFragment() {

    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.staggered_ad_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the current list of pinned ads
        viewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        // choose a Staggered layout to display the ads
        pinnedAdList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // gives the RecyclerView the adapter responsible to give  ads and display them
        pinnedAdList.adapter = adapter
        // Whenever the database is updated, room triggers an evetn through the LiveData list of ads and the new list is transferded to the adapter
        viewModel.allAds.observe(this, Observer { adapter.submitList(it) })
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}
