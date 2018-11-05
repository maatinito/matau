package pf.miki.matau.fragment.ads2

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ads2_fragment.*
import pf.miki.matau.helpers.AdInteractionListener

import pf.miki.matau.R
import pf.miki.matau.repository.AdRepository
import pf.miki.matau.repository.PAd

/*

class ViewModelFactory(private val repository: AdRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Ads2ViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return Ads2ViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
*/
class Ads2Fragment : Fragment() {

    companion object {
        fun newInstance(): Ads2Fragment {
            return Ads2Fragment()
        }
    }

    private lateinit var model: Ads2ViewModel
    private var adapter = AdPagedAdapter2 { model.retry() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ads2_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = ViewModelProviders.of(this).get(Ads2ViewModel::class.java)
        noAds.visibility = View.GONE
        adlist2.visibility = View.VISIBLE
        adlist2.layoutManager = LinearLayoutManager(this.context, LinearLayout.VERTICAL, false)
        initAdapter()
    }


    private fun initAdapter() {
        adlist2.adapter = adapter
        model.ads.observe(this, Observer<PagedList<PAd>> {
            adapter.submitList(it)
            val noResult = it == null || it.isEmpty()
            noAds.visibility = if (noResult) View.VISIBLE else View.INVISIBLE
            adlist2.visibility = if (noResult) View.INVISIBLE else View.VISIBLE
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
        model.updatedAds.observe(this, Observer {
            adapter.notifyChangedAds(it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AdInteractionListener)
            adapter.listener = context
        else {
            throw RuntimeException(context.toString() + " must implement " + AdInteractionListener::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        adapter.listener = null
    }

}
